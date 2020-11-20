package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.dto.PaymentLogDTO;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.pay.aliPay.config.AlipayConfig;
import com.github.pagehelper.PageHelper;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.PaymentService;
import com.enuos.live.service.ProductBackpackService;
import com.enuos.live.utils.RandomUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName PaymentServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/7
 * @Version V1.0
 **/
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserAccountAttachMapper accountAttachMapper;

    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Autowired
    private CustomerBalanceLogMapper customerBalanceLogMapper;

    @Autowired
    private ProductBackpackService productBackpackService;

    @Autowired
    private ProductFullGiftMapper productFullGiftMapper;

    @Autowired
    private ProductPriceMapper productSriceMapper;

    @Autowired
    private UserBillMapper userBillMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductServiceMapper productServiceMapper;

    @Autowired
    private UserFeign userFeign;

//    @Autowired
//    @Qualifier("eAsyncServiceExecutor")
//    private ExecutorService executorService;

    /**
     * @MethodName: goldPayment
     * @Description: TODO 钻石金币兑换
     * @Param: [userId：用户ID, productId：商品ID, amount：购买数量, paymentMethod：支付方式，sriceId：商品价格id]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/7
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result payment(Long userId, Long productId, Integer amount, Integer payType, Integer priceId) {

        // 查询用户金币和钻石数量
        Map<String, Object> map = accountAttachMapper.getBalance(null, userId);
        if (ObjectUtil.isEmpty(map)) {
            log.error("1040, 未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        final int id = MapUtil.getInt(map, "id"); // 用户账户Id
        final long gold = MapUtil.getLong(map, "gold"); // 用户金币
        final long diamond = MapUtil.getLong(map, "diamond"); // 用户钻石
        if (payType == 2 && diamond <= 0) {
            log.info("用户钻石不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }
        if (payType == 3 && gold == 0) {
            log.info("用户金币不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
        }

        // 商品信息
        ProductPayInfo productPayInfo = productInfoMapper.getProductPayInfo(productId, priceId);
        if (ObjectUtils.isEmpty(productPayInfo)) {
            log.info("商品不存在，未查询到商品：productId={}, userId={}, priceId={}", productId, userId, priceId);
            return Result.error(6001, "商品没了");
        }
        long priceBasic = 0L;
        if (productPayInfo.getPayType1() == payType) {
            priceBasic = productPayInfo.getPrice1();
        } else if (productPayInfo.getPayType2() == payType) {
            priceBasic = productPayInfo.getPrice2();
        } else {
            log.info("不支持的付款类型");
            return Result.error(6003, "不支持的付款类型");
        }
        if (priceBasic == 0) {
            log.info("价格为零(100)");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        long orderMoney = amount * priceBasic; //总价 = 商品数量*单价
        int full = productPayInfo.getFull();
        int gift = productPayInfo.getGift();
        UserAccountAttach accountAttach = new UserAccountAttach(); // 用户账户信息
        accountAttach.setId(id);
        OrderMsg order = new OrderMsg();
        // 判断购买物品类型
        if (productPayInfo.getOneCategoryId() == 2) { // 购买金币
            // 钻石付款
            long surplusDiamond = diamond - orderMoney; // 剩余钻石
            if (surplusDiamond < 0) {
                log.info("用户钻石不足：userId={}, diamond={}, orderMoney={}", userId, diamond, orderMoney);
                return Result.error(80011, "钻石余额不足，当前余额：" + diamond);
            }
            long rechargeQuota = 0L; //充值额度

            // 金币充值
            rechargeQuota = Long.valueOf(amount * (full + gift)); // 总数量
            final long surplusGold = gold + rechargeQuota; //要充值的金币
            accountAttach.setGold(surplusGold);
            accountAttach.setDiamond(surplusDiamond);
            accountAttachMapper.update(accountAttach); // 更新用户账户信息

            // 保存账单
            UserBill bill = new UserBill();
            bill.setUserId(userId);
            bill.setPrice(-orderMoney);
            bill.setPriceType(2); // 2钻石 3:金币
            bill.setProductName(rechargeQuota + "金币 兑换 ");
            bill.setStatus(1);
            userBillMapper.insert(bill);
//            executorService.submit(() -> {
//                userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(orderMoney)));
//            });
            new Thread(() -> {
                userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(orderMoney)));
            }).start();

        } else { // 购买游戏道具装饰等物品

            UserBill bill = new UserBill();
            bill.setUserId(userId);
            bill.setStatus(1);
            switch (payType) {
                case 2:
                    long surplusDiamond = diamond - orderMoney; // 扣除后钻石
                    if (surplusDiamond < 0) {
                        log.info("用户钻石不足：userId={}, diamond={}, orderMoney={}", userId, diamond, orderMoney);
                        return Result.error(80011, "钻石余额不足，当前余额：" + diamond);
                    }
                    accountAttach.setDiamond(surplusDiamond);
                    // 保存账单
                    bill.setPrice(-orderMoney);
                    bill.setPriceType(2); // 2钻石 3:金币
                    bill.setProductName(productPayInfo.getProductName() + " 兑换");
                    break;
                case 3:
                    if (orderMoney > gold) {
                        log.info("用户金币不足：userId={}", userId);
                        return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
                    }
                    final long surplusGold = gold - orderMoney; // 扣除后金币
                    accountAttach.setGold(surplusGold);
                    // 保存账单
                    bill.setPrice(-orderMoney);
                    bill.setPriceType(3); // 2钻石 3:金币
                    bill.setProductName(productPayInfo.getProductName() + " 兑换");
                    break;
                default:
                    log.info("不支持的付款类型");
                    return Result.error(6003, "不支持的付款类型");
            }

            // 添加背包
            ProductBackpack productBackpack = new ProductBackpack();
            productBackpack.setUserId(userId);
            productBackpack.setProductId(productId);
            productBackpack.setProductCode(productPayInfo.getProductCode());
            productBackpack.setGameLabelId(productPayInfo.getGameLabelId());
            productBackpack.setProductNum(amount);// 物品数量
            int timeLimit = productPayInfo.getTimeLimit();
            if (timeLimit == -1) {
                productBackpack.setTimeLimit(Long.valueOf(timeLimit));// 物品有效期限
            } else {
                productBackpack.setTimeLimit(Long.valueOf(timeLimit * 24 * 3600));// 物品有效期限 保存为秒值
            }
            productBackpack.setCategoryId(productPayInfo.getOneCategoryId()); // 分类ID
            productBackpack.setProductStatus(1); // 使用状态
            Result result = productBackpackService.upProductBackpack(productBackpack);
            if (result.getCode() != 0) {
//                throw new DataIntegrityViolationException("数据库异常");
                return result;
            }
            userBillMapper.insert(bill);

            accountAttach.setId(id);
            accountAttachMapper.update(accountAttach); // 更新用户账户信息

            if (payType == 2) {
                new Thread(() -> {
                    userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(orderMoney)));
                }).start();
            }
        }

        return Result.success();
    }

    /**
     * @MethodName: cashPayment
     * @Description: TODO 现金支付充值钻石，返回页面进行信息确认，去调用支付接口
     * @Param: [userId, productId, amount, paymentMethod]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/29
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result cashPayment(Long userId, Long productId, Integer amount, Integer payType, Integer priceId) {
        // 判断是否实名认证
        if (userFeign.isAuthentication(userId) == 0) {
            return Result.error(ErrorCode.USER_NO_AUTHENTICATION);
        }

        ProductPayInfo productPayInfo = productInfoMapper.getProductPayInfo(productId, priceId);
        if (ObjectUtils.isEmpty(productPayInfo)) {
            log.info("商品不存在，未查询到商品：productId={}, userId={}, priceId={}", productId, userId, priceId);
            return Result.error(6001, "商品没了");
        }
        // 校验支付方式
        if (payType != productPayInfo.getPayType1()) {
            log.info("不支持的付款类型");
            return Result.error(6003, "不支持的购买类型");
        }

        int full = productPayInfo.getFull();
        int gift = productPayInfo.getGift();
        Long priceBasic = productPayInfo.getPrice1();
        String productCode = productPayInfo.getProductCode();
        String productName = productPayInfo.getProductName();

        int productQuota = amount * (full + gift);
        String orderNo = "1" + RandomUtil.getRandom();// 生成订单号
        Long totalAmount = amount * priceBasic; // 总金额

        // 添加订单
        OrderMsg order = new OrderMsg();
        order.setOrderSn(Long.parseLong(orderNo));// 订单编号
        order.setUserId(userId); // 下单人ID
        order.setProductType(1); // 商品类型 1: 钻石 2:会员 3:其他
        order.setProductId(productId); // 商品ID
        order.setProductCode(productCode); // 商品编码
        order.setProductName(productName); // 商品名称
        order.setProductCnt(amount); // 商品数量
        order.setProductQuota(productQuota); // 额度
        order.setPaymentMethod(1); // 支付方式：1：人民币，2：钻石，3：金币
        order.setRmbMethod(null); // 现金支付方式：1：支付宝 2：微信 3：银联 4：ApplePay 5:其他
        order.setOrderMoney(totalAmount.toString()); // 订单金额
        order.setPaymentMoney(null); // 支付金额
        order.setOrderStatus(2); // 订单状态：1：交易成功  2：待支付 3：交易失败
        int status = orderMapper.insert(order);
        if (status <= 0) {
            throw new DataIntegrityViolationException("数据库异常");
        }
        Map orderInfo = new HashMap();
        orderInfo.put("userId", userId); // 用户ID
        orderInfo.put("orderNo", orderNo); // 订单编号
        orderInfo.put("amount", amount); // 购买数量
        orderInfo.put("productName", productName); // 商品名称
        orderInfo.put("productCode", productCode); // 商品编码
        orderInfo.put("totalDiamond", productQuota); // 充值数量
        orderInfo.put("totalAmount", totalAmount.toString()); // 总金额
        return Result.success(orderInfo);
    }

    /**
     * @MethodName: paymentVIP
     * @Description: TODO 会员充值预订单
     * @Param: [userId, productId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:50 2020/7/10
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result paymentVIP(Long userId, String productId) {
        // 判断是否实名认证
        if (userFeign.isAuthentication(userId) == 0) {
            return Result.error(ErrorCode.USER_NO_AUTHENTICATION);
        }

        List<Map<String, Object>> maps = productInfoMapper.getRechargePackage(productId);
        Map<String, Object> map = maps.get(0);
        int amount = MapUtil.getInt(map, "duration");
        long id = MapUtil.getLong(map, "id");
        String productName = "会员充值-" + MapUtil.getStr(map, "name");
        String productCode = MapUtil.getStr(map, "productCode");
        String price = MapUtil.getStr(map, "price");

        String orderNo = "2" + RandomUtil.getRandom();// 生成订单号

        // 添加订单
        OrderMsg order = new OrderMsg();
        order.setOrderSn(Long.parseLong(orderNo));// 订单编号
        order.setUserId(userId); // 下单人ID
        order.setProductType(2); // 商品类型 1: 钻石 2:会员 3:其他
        order.setProductId(id); // 商品ID
        order.setProductCode(productId); // 商品编码
        order.setProductName(productName); // 商品名称
        order.setProductCnt(amount); // 商品数量
        order.setProductQuota(amount); // 额度
        order.setPaymentMethod(1); // 支付方式：1：人民币，2：钻石，3：金币
        order.setRmbMethod(null); // 现金支付方式：1：支付宝 2：微信 3：银联 4：ApplePay 5:其他
        order.setOrderMoney(price); // 订单金额
        order.setPaymentMoney(null); // 支付金额
        order.setOrderStatus(2); // 订单状态：1：交易成功  2：待支付 3：交易失败
        int status = orderMapper.insert(order);
        if (status <= 0) {
            throw new DataIntegrityViolationException("数据库异常");
        }

        Map orderInfo = new HashMap();
        orderInfo.put("userId", userId); // 用户ID
        orderInfo.put("orderNo", orderNo); // 订单编号
        orderInfo.put("amount", amount); // 购买数量
        orderInfo.put("productName", productName); // 商品名称
        orderInfo.put("productCode", productCode); // 商品编码
        orderInfo.put("totalDiamond", amount); // 充值数量
        orderInfo.put("totalAmount", price); // 总金额
        return Result.success(orderInfo);
    }


    /**
     * @MethodName: diamondRecharge
     * @Description: TODO 支付成功后 钻石充值
     * @Param: [order]
     * @Return: void
     * @Author: xubin
     * @Date: 10:11 2020/7/10
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Async
    @Override
    public void diamondRecharge(OrderMsg order) {
        int diamond = order.getProductQuota();// 充值的钻石总量
        Long userId = order.getUserId(); // 用户ID
        UserAccountAttach accountAttach = new UserAccountAttach();
        accountAttach.setUserId(userId);
        accountAttach.setDiamond(Long.valueOf(diamond));
        Integer integer = accountAttachMapper.updateDiamond(accountAttach);
        if (integer > 0) {
            order.setIsHandle(1);
        } else {
            order.setIsHandle(2);
        }
        int i = orderMapper.updateHandle(order.getOrderSn(), order.getIsHandle());
        log.info("钻石充值处理结果,handle=[{}]", i);
        // 保存账单
        UserBill bill = new UserBill();
        bill.setUserId(userId);
        bill.setPrice(Long.valueOf(diamond));
        bill.setPriceType(2); // 2钻石 3:金币
        bill.setProductName("钻石充值");
        bill.setStatus(1);
        int insert = userBillMapper.insert(bill);
        log.info("保存账单状态=[{}]", insert);
    }


    /**
     * @MethodName: diamondExGold
     * @Description: TODO 自定义钻石兑换金币
     * @Param: [userId, gold]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:07 2020/6/22
     **/
    @Override
    public Result diamondExGold(Long userId, Long exGold) {
        //查询用户金币
        Map<String, Object> map = accountAttachMapper.getBalance(null, userId);
        if (map.size() <= 0) {
            log.info("未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        int id = (Integer) map.get("id"); // 用户账户Id
        long gold = (Long) map.get("gold"); // 用户金币
        long diamond = (Long) map.get("diamond"); // 用户钻石

        double goldNum = exGold; // 金币类型转换
        int ratio = accountAttachMapper.selectByPrimaryKeyGoldRatio(exGold);
        double flag = 0;
        long result1 = 0; // 需要的钻石
        flag = Math.ceil(goldNum / ratio); // 向上取整计算
        result1 = (long) flag;

        long surplusDiamond = diamond - result1;
        if (surplusDiamond < 0) {
            log.info("钻石不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }
        long totalGold = gold + exGold;

        UserAccountAttach accountAttach = new UserAccountAttach();
        accountAttach.setId(id);
        accountAttach.setDiamond(surplusDiamond);
        accountAttach.setGold(totalGold);
        accountAttachMapper.update(accountAttach);
        accountAttach.setUserId(userId);

        UserBill bill = new UserBill();
        bill.setUserId(userId);
        bill.setPrice(-result1);
        bill.setPriceType(2);
        bill.setProductName("金币兑换 " + exGold);
        bill.setStatus(1);
        userBillMapper.insert(bill);
        long finalResult = result1;
//        executorService.submit(() -> {
//            userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(finalResult)));
//        });
        new Thread(() -> {
            userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(finalResult)));
        }).start();
        return Result.success(accountAttach);
    }

    @Override
    public Result paymentLog(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return Result.success();
    }

}
