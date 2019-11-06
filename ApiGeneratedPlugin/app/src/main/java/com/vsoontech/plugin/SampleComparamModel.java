package com.vsoontech.plugin;

import com.vsoontech.plugin.api.SampleComparamResp;
import java.util.ArrayList;

public class SampleComparamModel {

    // 状态码 ; NotNull : true
    public String code;
    // 所有优惠券 ; NotNull : true
    public ArrayList<Coupon> couponList;
    // 可用书列表 ; NotNull : true
    public BookName bookName;

    public SampleComparamModel(SampleComparamResp samplecomparamresp) {
        this.code = samplecomparamresp.code;
        this.bookName = new BookName(samplecomparamresp.bookName);
        if (samplecomparamresp.couponList != null && !samplecomparamresp.couponList.isEmpty()) {
            this.couponList = new ArrayList<>();
            for (SampleComparamResp.Coupon coupon : samplecomparamresp.couponList) {
                this.couponList.add(new Coupon(coupon));
            }
        }

        onConstructor();
    }

    void onConstructor() {// do Something
    }

    public static class Coupon {

        // 优惠券-标题 ; NotNull : true
        public String name;
        // 优惠券-可抵用差价 ; NotNull : true
        public int price;

        public Coupon(SampleComparamResp.Coupon coupon) {
            this.name = coupon.name;
            this.price = coupon.price;
            onConstructor();
        }

        void onConstructor() {// do Something
        }

        @Override
        public String toString() {
            return "Coupon{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
        }
    }

    public static class BookName {

        // 书名 ; NotNull : true
        public String name;
        // 价格 ; NotNull : true
        public float price;
        // 可销售量-不包含库存 ; NotNull : true
        public int saleNum;
        // 可用优惠券 ; NotNull : true
        public ArrayList<Coupon> couponList;

        public BookName(SampleComparamResp.BookName bookname) {
            this.name = bookname.name;
            this.price = bookname.price;
            this.saleNum = bookname.saleNum;
            if (bookname.couponList != null && !bookname.couponList.isEmpty()) {
                this.couponList = new ArrayList<>();
                for (SampleComparamResp.Coupon coupon : bookname.couponList) {
                    this.couponList.add(new Coupon(coupon));
                }
            }

            onConstructor();
        }

        void onConstructor() {// do Something
        }

        @Override
        public String toString() {
            return "BookName{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", saleNum=" + saleNum +
                ", couponList=" + couponList +
                '}';
        }
    }

    @Override
    public String toString() {
        return "SampleComparamModel{" +
            "code='" + code + '\'' +
            ", couponList=" + couponList +
            ", bookName=" + bookName +
            '}';
    }
}
