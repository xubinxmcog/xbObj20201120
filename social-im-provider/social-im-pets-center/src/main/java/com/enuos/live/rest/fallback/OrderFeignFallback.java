//package com.enuos.live.rest.fallback;
//
//import com.enuos.live.error.ErrorCode;
//import com.enuos.live.rest.OrderFeign;
//import com.enuos.live.result.Result;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * @ClassName OrderFeignFallback
// * @Description: TODO
// * @Author xubin
// * @Date 2020/6/18
// * @Version V2.0
// **/
//@Slf4j
//@Component
//public class OrderFeignFallback implements OrderFeign {
//    @Override
//    public Result entryBill(Map<String, Object> params) {
//        log.error("入账异常:params={}", params);
//        return Result.error(ErrorCode.NETWORK_ERROR);
//    }
//}
