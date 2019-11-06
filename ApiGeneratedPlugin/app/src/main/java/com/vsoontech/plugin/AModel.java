package com.vsoontech.plugin;

import com.vsoontech.plugin.api.AddressConsts;
import com.vsoontech.plugin.api.AddressResp;
import java.util.ArrayList;

/**
 * Automatically generated file. DO NOT MODIFY !
 * : 移动端直播 - 安卓用户维护收货地址
 *
 * @author ApiGenerate
 * @since 2019-11-06 10:09
 */
public class AModel {

    // 1：成功；0：失败
    private int result;
    /**
     * op字符串常量<br>
     * 说明: {@link AddressConsts#OP_STR} ; <br>
     */
    private String opStr;
    /**
     * 操作类型<br>
     * 获取: {@link AddressConsts#OPTION_GET} ; <br>
     * 添加: {@link AddressConsts#OPTION_ADD} ; <br>
     * 修改: {@link AddressConsts#OPTION_MODIFY} ; <br>
     * 删除: {@link AddressConsts#OPTION_DELETE} ; <br>
     * 其他: {@link AddressConsts#OPTION_OTHER} ; <br>
     */
    private int op;
    // 增删改返回的地址id
    private int opId;
    // list
    private ArrayList<Item> list;
    /**
     * op其他<br>
     * op其他: {@link AddressConsts#OP_OTHER_INT} ; <br>
     */
    private int opOther;

    public AModel(AddressResp addressresp) {
        this.op = addressresp.op;
        this.result = addressresp.result;
        this.opId = addressresp.opId;
        if (addressresp.list != null && !addressresp.list.isEmpty()) {
            this.list = new ArrayList<>();
            for (AddressResp.Item item : addressresp.list) {
                this.list.add(new Item(item));
            }
        }
        this.opStr = addressresp.opStr;
        this.opOther = addressresp.opOther;
        onAModelConstructor();
    }

    /**
     * 为避免构造函数内非插件生成的代码被覆盖，如有特殊操作，可将代码添加到此方法中;
     */
    private void onAModelConstructor() {
    }

    public int getResult() {
        return result;
    }

    public String getOpStr() {
        return opStr;
    }

    public int getOp() {
        return op;
    }

    public int getOpId() {
        return opId;
    }

    public ArrayList<Item> getList() {
        return list;
    }

    public int getOpOther() {
        return opOther;
    }

    /**
     * Automatically generated file. DO NOT MODIFY !
     */
    public static class Item {

        // 数据id
        private int id;
        // 收货人
        private String consignee;
        // 邮政编码
        private String postalCode;
        // 省
        private String province;
        // 市
        private String city;
        // 区
        private String country;
        // 详细地址
        private String detail;
        // 电话
        private String telNumber;

        public Item(AddressResp.Item item) {
            this.id = item.id;
            this.consignee = item.consignee;
            this.postalCode = item.postalCode;
            this.province = item.province;
            this.city = item.city;
            this.country = item.country;
            this.detail = item.detail;
            this.telNumber = item.telNumber;
            onItemConstructor();
        }

        /**
         * 为避免构造函数内非插件生成的代码被覆盖，如有特殊操作，可将代码添加到此方法中;
         */
        private void onItemConstructor() {
        }

        public int getId() {
            return id;
        }

        public String getConsignee() {
            return consignee;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getProvince() {
            return province;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }

        public String getDetail() {
            return detail;
        }

        public String getTelNumber() {
            return telNumber;
        }
    }
}
