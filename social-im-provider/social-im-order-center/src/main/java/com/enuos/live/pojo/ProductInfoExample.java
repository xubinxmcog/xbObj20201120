package com.enuos.live.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ProductInfoExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andProduct_coreIsNull() {
            addCriterion("product_code is null");
            return (Criteria) this;
        }

        public Criteria andProduct_coreIsNotNull() {
            addCriterion("product_code is not null");
            return (Criteria) this;
        }

        public Criteria andProduct_coreEqualTo(String value) {
            addCriterion("product_code =", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreNotEqualTo(String value) {
            addCriterion("product_code <>", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreGreaterThan(String value) {
            addCriterion("product_code >", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreGreaterThanOrEqualTo(String value) {
            addCriterion("product_code >=", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreLessThan(String value) {
            addCriterion("product_code <", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreLessThanOrEqualTo(String value) {
            addCriterion("product_code <=", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreLike(String value) {
            addCriterion("product_code like", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreNotLike(String value) {
            addCriterion("product_code not like", value, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreIn(List<String> values) {
            addCriterion("product_code in", values, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreNotIn(List<String> values) {
            addCriterion("product_code not in", values, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreBetween(String value1, String value2) {
            addCriterion("product_code between", value1, value2, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_coreNotBetween(String value1, String value2) {
            addCriterion("product_code not between", value1, value2, "product_code");
            return (Criteria) this;
        }

        public Criteria andProduct_nameIsNull() {
            addCriterion("product_name is null");
            return (Criteria) this;
        }

        public Criteria andProduct_nameIsNotNull() {
            addCriterion("product_name is not null");
            return (Criteria) this;
        }

        public Criteria andProduct_nameEqualTo(String value) {
            addCriterion("product_name =", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameNotEqualTo(String value) {
            addCriterion("product_name <>", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameGreaterThan(String value) {
            addCriterion("product_name >", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameGreaterThanOrEqualTo(String value) {
            addCriterion("product_name >=", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameLessThan(String value) {
            addCriterion("product_name <", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameLessThanOrEqualTo(String value) {
            addCriterion("product_name <=", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameLike(String value) {
            addCriterion("product_name like", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameNotLike(String value) {
            addCriterion("product_name not like", value, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameIn(List<String> values) {
            addCriterion("product_name in", values, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameNotIn(List<String> values) {
            addCriterion("product_name not in", values, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameBetween(String value1, String value2) {
            addCriterion("product_name between", value1, value2, "product_name");
            return (Criteria) this;
        }

        public Criteria andProduct_nameNotBetween(String value1, String value2) {
            addCriterion("product_name not between", value1, value2, "product_name");
            return (Criteria) this;
        }

        public Criteria andOne_category_idIsNull() {
            addCriterion("one_category_id is null");
            return (Criteria) this;
        }

        public Criteria andOne_category_idIsNotNull() {
            addCriterion("one_category_id is not null");
            return (Criteria) this;
        }

        public Criteria andOne_category_idEqualTo(Short value) {
            addCriterion("one_category_id =", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idNotEqualTo(Short value) {
            addCriterion("one_category_id <>", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idGreaterThan(Short value) {
            addCriterion("one_category_id >", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idGreaterThanOrEqualTo(Short value) {
            addCriterion("one_category_id >=", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idLessThan(Short value) {
            addCriterion("one_category_id <", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idLessThanOrEqualTo(Short value) {
            addCriterion("one_category_id <=", value, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idIn(List<Short> values) {
            addCriterion("one_category_id in", values, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idNotIn(List<Short> values) {
            addCriterion("one_category_id not in", values, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idBetween(Short value1, Short value2) {
            addCriterion("one_category_id between", value1, value2, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andOne_category_idNotBetween(Short value1, Short value2) {
            addCriterion("one_category_id not between", value1, value2, "one_category_id");
            return (Criteria) this;
        }

        public Criteria andPriceIsNull() {
            addCriterion("price is null");
            return (Criteria) this;
        }

        public Criteria andPriceIsNotNull() {
            addCriterion("price is not null");
            return (Criteria) this;
        }

        public Criteria andPriceEqualTo(Integer value) {
            addCriterion("price =", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotEqualTo(Integer value) {
            addCriterion("price <>", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceGreaterThan(Integer value) {
            addCriterion("price >", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceGreaterThanOrEqualTo(Integer value) {
            addCriterion("price >=", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceLessThan(Integer value) {
            addCriterion("price <", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceLessThanOrEqualTo(Integer value) {
            addCriterion("price <=", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceIn(List<Integer> values) {
            addCriterion("price in", values, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotIn(List<Integer> values) {
            addCriterion("price not in", values, "price");
            return (Criteria) this;
        }

        public Criteria andPriceBetween(Integer value1, Integer value2) {
            addCriterion("price between", value1, value2, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotBetween(Integer value1, Integer value2) {
            addCriterion("price not between", value1, value2, "price");
            return (Criteria) this;
        }

        public Criteria andAverage_costIsNull() {
            addCriterion("average_cost is null");
            return (Criteria) this;
        }

        public Criteria andAverage_costIsNotNull() {
            addCriterion("average_cost is not null");
            return (Criteria) this;
        }

        public Criteria andAverage_costEqualTo(Integer value) {
            addCriterion("average_cost =", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costNotEqualTo(Integer value) {
            addCriterion("average_cost <>", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costGreaterThan(Integer value) {
            addCriterion("average_cost >", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costGreaterThanOrEqualTo(Integer value) {
            addCriterion("average_cost >=", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costLessThan(Integer value) {
            addCriterion("average_cost <", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costLessThanOrEqualTo(Integer value) {
            addCriterion("average_cost <=", value, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costIn(List<Integer> values) {
            addCriterion("average_cost in", values, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costNotIn(List<Integer> values) {
            addCriterion("average_cost not in", values, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costBetween(Integer value1, Integer value2) {
            addCriterion("average_cost between", value1, value2, "average_cost");
            return (Criteria) this;
        }

        public Criteria andAverage_costNotBetween(Integer value1, Integer value2) {
            addCriterion("average_cost not between", value1, value2, "average_cost");
            return (Criteria) this;
        }

        public Criteria andPublish_statusIsNull() {
            addCriterion("publish_status is null");
            return (Criteria) this;
        }

        public Criteria andPublish_statusIsNotNull() {
            addCriterion("publish_status is not null");
            return (Criteria) this;
        }

        public Criteria andPublish_statusEqualTo(Byte value) {
            addCriterion("publish_status =", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusNotEqualTo(Byte value) {
            addCriterion("publish_status <>", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusGreaterThan(Byte value) {
            addCriterion("publish_status >", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusGreaterThanOrEqualTo(Byte value) {
            addCriterion("publish_status >=", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusLessThan(Byte value) {
            addCriterion("publish_status <", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusLessThanOrEqualTo(Byte value) {
            addCriterion("publish_status <=", value, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusIn(List<Byte> values) {
            addCriterion("publish_status in", values, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusNotIn(List<Byte> values) {
            addCriterion("publish_status not in", values, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusBetween(Byte value1, Byte value2) {
            addCriterion("publish_status between", value1, value2, "publish_status");
            return (Criteria) this;
        }

        public Criteria andPublish_statusNotBetween(Byte value1, Byte value2) {
            addCriterion("publish_status not between", value1, value2, "publish_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusIsNull() {
            addCriterion("audit_status is null");
            return (Criteria) this;
        }

        public Criteria andAudit_statusIsNotNull() {
            addCriterion("audit_status is not null");
            return (Criteria) this;
        }

        public Criteria andAudit_statusEqualTo(Byte value) {
            addCriterion("audit_status =", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusNotEqualTo(Byte value) {
            addCriterion("audit_status <>", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusGreaterThan(Byte value) {
            addCriterion("audit_status >", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusGreaterThanOrEqualTo(Byte value) {
            addCriterion("audit_status >=", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusLessThan(Byte value) {
            addCriterion("audit_status <", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusLessThanOrEqualTo(Byte value) {
            addCriterion("audit_status <=", value, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusIn(List<Byte> values) {
            addCriterion("audit_status in", values, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusNotIn(List<Byte> values) {
            addCriterion("audit_status not in", values, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusBetween(Byte value1, Byte value2) {
            addCriterion("audit_status between", value1, value2, "audit_status");
            return (Criteria) this;
        }

        public Criteria andAudit_statusNotBetween(Byte value1, Byte value2) {
            addCriterion("audit_status not between", value1, value2, "audit_status");
            return (Criteria) this;
        }

        public Criteria andWeightIsNull() {
            addCriterion("weight is null");
            return (Criteria) this;
        }

        public Criteria andWeightIsNotNull() {
            addCriterion("weight is not null");
            return (Criteria) this;
        }

        public Criteria andWeightEqualTo(String value) {
            addCriterion("weight =", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotEqualTo(String value) {
            addCriterion("weight <>", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightGreaterThan(String value) {
            addCriterion("weight >", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightGreaterThanOrEqualTo(String value) {
            addCriterion("weight >=", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightLessThan(String value) {
            addCriterion("weight <", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightLessThanOrEqualTo(String value) {
            addCriterion("weight <=", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightLike(String value) {
            addCriterion("weight like", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotLike(String value) {
            addCriterion("weight not like", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightIn(List<String> values) {
            addCriterion("weight in", values, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotIn(List<String> values) {
            addCriterion("weight not in", values, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightBetween(String value1, String value2) {
            addCriterion("weight between", value1, value2, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotBetween(String value1, String value2) {
            addCriterion("weight not between", value1, value2, "weight");
            return (Criteria) this;
        }

        public Criteria andLengthIsNull() {
            addCriterion("`length` is null");
            return (Criteria) this;
        }

        public Criteria andLengthIsNotNull() {
            addCriterion("`length` is not null");
            return (Criteria) this;
        }

        public Criteria andLengthEqualTo(String value) {
            addCriterion("`length` =", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthNotEqualTo(String value) {
            addCriterion("`length` <>", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthGreaterThan(String value) {
            addCriterion("`length` >", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthGreaterThanOrEqualTo(String value) {
            addCriterion("`length` >=", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthLessThan(String value) {
            addCriterion("`length` <", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthLessThanOrEqualTo(String value) {
            addCriterion("`length` <=", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthLike(String value) {
            addCriterion("`length` like", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthNotLike(String value) {
            addCriterion("`length` not like", value, "length");
            return (Criteria) this;
        }

        public Criteria andLengthIn(List<String> values) {
            addCriterion("`length` in", values, "length");
            return (Criteria) this;
        }

        public Criteria andLengthNotIn(List<String> values) {
            addCriterion("`length` not in", values, "length");
            return (Criteria) this;
        }

        public Criteria andLengthBetween(String value1, String value2) {
            addCriterion("`length` between", value1, value2, "length");
            return (Criteria) this;
        }

        public Criteria andLengthNotBetween(String value1, String value2) {
            addCriterion("`length` not between", value1, value2, "length");
            return (Criteria) this;
        }

        public Criteria andHeightIsNull() {
            addCriterion("height is null");
            return (Criteria) this;
        }

        public Criteria andHeightIsNotNull() {
            addCriterion("height is not null");
            return (Criteria) this;
        }

        public Criteria andHeightEqualTo(String value) {
            addCriterion("height =", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightNotEqualTo(String value) {
            addCriterion("height <>", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightGreaterThan(String value) {
            addCriterion("height >", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightGreaterThanOrEqualTo(String value) {
            addCriterion("height >=", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightLessThan(String value) {
            addCriterion("height <", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightLessThanOrEqualTo(String value) {
            addCriterion("height <=", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightLike(String value) {
            addCriterion("height like", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightNotLike(String value) {
            addCriterion("height not like", value, "height");
            return (Criteria) this;
        }

        public Criteria andHeightIn(List<String> values) {
            addCriterion("height in", values, "height");
            return (Criteria) this;
        }

        public Criteria andHeightNotIn(List<String> values) {
            addCriterion("height not in", values, "height");
            return (Criteria) this;
        }

        public Criteria andHeightBetween(String value1, String value2) {
            addCriterion("height between", value1, value2, "height");
            return (Criteria) this;
        }

        public Criteria andHeightNotBetween(String value1, String value2) {
            addCriterion("height not between", value1, value2, "height");
            return (Criteria) this;
        }

        public Criteria andWidthIsNull() {
            addCriterion("width is null");
            return (Criteria) this;
        }

        public Criteria andWidthIsNotNull() {
            addCriterion("width is not null");
            return (Criteria) this;
        }

        public Criteria andWidthEqualTo(String value) {
            addCriterion("width =", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthNotEqualTo(String value) {
            addCriterion("width <>", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthGreaterThan(String value) {
            addCriterion("width >", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthGreaterThanOrEqualTo(String value) {
            addCriterion("width >=", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthLessThan(String value) {
            addCriterion("width <", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthLessThanOrEqualTo(String value) {
            addCriterion("width <=", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthLike(String value) {
            addCriterion("width like", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthNotLike(String value) {
            addCriterion("width not like", value, "width");
            return (Criteria) this;
        }

        public Criteria andWidthIn(List<String> values) {
            addCriterion("width in", values, "width");
            return (Criteria) this;
        }

        public Criteria andWidthNotIn(List<String> values) {
            addCriterion("width not in", values, "width");
            return (Criteria) this;
        }

        public Criteria andWidthBetween(String value1, String value2) {
            addCriterion("width between", value1, value2, "width");
            return (Criteria) this;
        }

        public Criteria andWidthNotBetween(String value1, String value2) {
            addCriterion("width not between", value1, value2, "width");
            return (Criteria) this;
        }

        public Criteria andColor_typeIsNull() {
            addCriterion("color_type is null");
            return (Criteria) this;
        }

        public Criteria andColor_typeIsNotNull() {
            addCriterion("color_type is not null");
            return (Criteria) this;
        }

        public Criteria andColor_typeEqualTo(String value) {
            addCriterion("color_type =", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeNotEqualTo(String value) {
            addCriterion("color_type <>", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeGreaterThan(String value) {
            addCriterion("color_type >", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeGreaterThanOrEqualTo(String value) {
            addCriterion("color_type >=", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeLessThan(String value) {
            addCriterion("color_type <", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeLessThanOrEqualTo(String value) {
            addCriterion("color_type <=", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeLike(String value) {
            addCriterion("color_type like", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeNotLike(String value) {
            addCriterion("color_type not like", value, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeIn(List<String> values) {
            addCriterion("color_type in", values, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeNotIn(List<String> values) {
            addCriterion("color_type not in", values, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeBetween(String value1, String value2) {
            addCriterion("color_type between", value1, value2, "color_type");
            return (Criteria) this;
        }

        public Criteria andColor_typeNotBetween(String value1, String value2) {
            addCriterion("color_type not between", value1, value2, "color_type");
            return (Criteria) this;
        }

        public Criteria andDescriptIsNull() {
            addCriterion("descript is null");
            return (Criteria) this;
        }

        public Criteria andDescriptIsNotNull() {
            addCriterion("descript is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptEqualTo(String value) {
            addCriterion("descript =", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptNotEqualTo(String value) {
            addCriterion("descript <>", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptGreaterThan(String value) {
            addCriterion("descript >", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptGreaterThanOrEqualTo(String value) {
            addCriterion("descript >=", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptLessThan(String value) {
            addCriterion("descript <", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptLessThanOrEqualTo(String value) {
            addCriterion("descript <=", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptLike(String value) {
            addCriterion("descript like", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptNotLike(String value) {
            addCriterion("descript not like", value, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptIn(List<String> values) {
            addCriterion("descript in", values, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptNotIn(List<String> values) {
            addCriterion("descript not in", values, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptBetween(String value1, String value2) {
            addCriterion("descript between", value1, value2, "descript");
            return (Criteria) this;
        }

        public Criteria andDescriptNotBetween(String value1, String value2) {
            addCriterion("descript not between", value1, value2, "descript");
            return (Criteria) this;
        }

        public Criteria andIndate_timeIsNull() {
            addCriterion("indate_time is null");
            return (Criteria) this;
        }

        public Criteria andIndate_timeIsNotNull() {
            addCriterion("indate_time is not null");
            return (Criteria) this;
        }

        public Criteria andIndate_timeEqualTo(Date value) {
            addCriterion("indate_time =", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeNotEqualTo(Date value) {
            addCriterion("indate_time <>", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeGreaterThan(Date value) {
            addCriterion("indate_time >", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeGreaterThanOrEqualTo(Date value) {
            addCriterion("indate_time >=", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeLessThan(Date value) {
            addCriterion("indate_time <", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeLessThanOrEqualTo(Date value) {
            addCriterion("indate_time <=", value, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeIn(List<Date> values) {
            addCriterion("indate_time in", values, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeNotIn(List<Date> values) {
            addCriterion("indate_time not in", values, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeBetween(Date value1, Date value2) {
            addCriterion("indate_time between", value1, value2, "indate_time");
            return (Criteria) this;
        }

        public Criteria andIndate_timeNotBetween(Date value1, Date value2) {
            addCriterion("indate_time not between", value1, value2, "indate_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeIsNull() {
            addCriterion("modified_time is null");
            return (Criteria) this;
        }

        public Criteria andModified_timeIsNotNull() {
            addCriterion("modified_time is not null");
            return (Criteria) this;
        }

        public Criteria andModified_timeEqualTo(Date value) {
            addCriterion("modified_time =", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeNotEqualTo(Date value) {
            addCriterion("modified_time <>", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeGreaterThan(Date value) {
            addCriterion("modified_time >", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeGreaterThanOrEqualTo(Date value) {
            addCriterion("modified_time >=", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeLessThan(Date value) {
            addCriterion("modified_time <", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeLessThanOrEqualTo(Date value) {
            addCriterion("modified_time <=", value, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeIn(List<Date> values) {
            addCriterion("modified_time in", values, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeNotIn(List<Date> values) {
            addCriterion("modified_time not in", values, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeBetween(Date value1, Date value2) {
            addCriterion("modified_time between", value1, value2, "modified_time");
            return (Criteria) this;
        }

        public Criteria andModified_timeNotBetween(Date value1, Date value2) {
            addCriterion("modified_time not between", value1, value2, "modified_time");
            return (Criteria) this;
        }
    }

    /**
     */
    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}