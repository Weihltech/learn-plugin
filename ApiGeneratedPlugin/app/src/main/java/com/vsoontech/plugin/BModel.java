package com.vsoontech.plugin;

import com.vsoontech.plugin.api.LiveCourseList2Resp;
import com.vsoontech.plugin.api.LiveMaybeList1Resp;
import com.vsoontech.plugin.api.LiveMaybeListResp;
import java.util.ArrayList;

public class BModel {

    // 推荐列表 ; NotNull : true
    public ArrayList<Recommend> recommendList;
    // 购买列表 ; NotNull : true
    public ArrayList<Purchased> purchasedList;

    public BModel(LiveMaybeListResp livemaybelistresp) {
        if (livemaybelistresp.purchasedList != null && !livemaybelistresp.purchasedList.isEmpty()) {
            this.purchasedList = new ArrayList<>();
            for (LiveMaybeListResp.Purchased purchased : livemaybelistresp.purchasedList) {
                this.purchasedList.add(new Purchased(purchased));
            }
        }
        if (livemaybelistresp.recommendList != null && !livemaybelistresp.recommendList.isEmpty()) {
            this.recommendList = new ArrayList<>();
            for (LiveMaybeListResp.Recommend recommend : livemaybelistresp.recommendList) {
                this.recommendList.add(new Recommend(recommend));
            }
        }

        onConstructor();
    }

    public BModel(LiveMaybeList1Resp livemaybelist1resp) {
        if (livemaybelist1resp.purchasedList != null && !livemaybelist1resp.purchasedList.isEmpty()) {
            this.purchasedList = new ArrayList<>();
            for (LiveMaybeList1Resp.Purchased purchased : livemaybelist1resp.purchasedList) {
                this.purchasedList.add(new Purchased(purchased));
            }
        }
        if (livemaybelist1resp.recommendList != null && !livemaybelist1resp.recommendList.isEmpty()) {
            this.recommendList = new ArrayList<>();
            for (LiveMaybeList1Resp.Recommend recommend : livemaybelist1resp.recommendList) {
                this.recommendList.add(new Recommend(recommend));
            }
        }

        onConstructor();
    }

    public BModel(LiveCourseList2Resp livecourselist2resp) {
        if (livecourselist2resp.purchasedList != null && !livecourselist2resp.purchasedList.isEmpty()) {
            this.purchasedList = new ArrayList<>();
            for (LiveCourseList2Resp.Purchased purchased : livecourselist2resp.purchasedList) {
                this.purchasedList.add(new Purchased(purchased));
            }
        }
        if (livecourselist2resp.recommendList != null && !livecourselist2resp.recommendList.isEmpty()) {
            this.recommendList = new ArrayList<>();
            for (LiveCourseList2Resp.Recommend recommend : livecourselist2resp.recommendList) {
                this.recommendList.add(new Recommend(recommend));
            }
        }

        onConstructor();
    }

    void onConstructor() {// do Something
    }

    public class Recommend {

        // 开课时间 ; NotNull : true
        public String openTime;
        // 课程名称 ; NotNull : true
        public String title;
        // 是否启用优惠价 [true - 用, false - 不用 ; NotNull : true
        public String isDiscount;
        // 教师列表 ; NotNull : true
        public ArrayList<Teacher> teacherList;
        // 教授 --- 测试重用对象 ; NotNull : true
        public Teacher proList;

        public Recommend(LiveMaybeListResp.Recommend recommend) {
            this.openTime = recommend.openTime;
            this.title = recommend.title;
            this.isDiscount = recommend.isDiscount;
            if (recommend.teacherList != null && !recommend.teacherList.isEmpty()) {
                this.teacherList = new ArrayList<>();
                for (LiveMaybeListResp.Teacher teacher : recommend.teacherList) {
                    this.teacherList.add(new Teacher(teacher));
                }
            }

            onConstructor();
        }

        public Recommend(LiveMaybeList1Resp.Recommend recommend) {
            this.openTime = recommend.openTime;
            this.title = recommend.title;
            this.isDiscount = recommend.isDiscount;
            if (recommend.teacherList != null && !recommend.teacherList.isEmpty()) {
                this.teacherList = new ArrayList<>();
                for (LiveMaybeList1Resp.Teacher teacher : recommend.teacherList) {
                    this.teacherList.add(new Teacher(teacher));
                }
            }

            onConstructor();
        }

        public Recommend(LiveCourseList2Resp.Recommend recommend) {
            this.openTime = recommend.openTime;
            this.title = recommend.title;
            this.isDiscount = recommend.isDiscount;
            if (recommend.teacherList != null && !recommend.teacherList.isEmpty()) {
                this.teacherList = new ArrayList<>();
                for (LiveCourseList2Resp.Teacher teacher : recommend.teacherList) {
                    this.teacherList.add(new Teacher(teacher));
                }
            }
            this.proList = new Teacher(recommend.proList);
            onConstructor();
        }

        void onConstructor() {// do Something
        }
    }

    public class Teacher {

        // 1主讲老师  2辅导老师 ; NotNull : true
        public int type;
        // 老师名字 ; NotNull : true
        public String name;

        public Teacher(LiveMaybeListResp.Teacher teacher) {
            this.type = teacher.type;
            this.name = teacher.name;
            onConstructor();
        }

        public Teacher(LiveMaybeList1Resp.Teacher teacher) {
            this.type = teacher.type;
            this.name = teacher.name;
            onConstructor();
        }

        public Teacher(LiveCourseList2Resp.Teacher teacher) {
            this.type = teacher.type;
            this.name = teacher.name;
            onConstructor();
        }

        void onConstructor() {// do Something
        }
    }

    public class Purchased {

        // 课程id ; NotNull : true
        public int liveCourseId;
        // 科目 ; NotNull : true
        public String subject;
        // 是否神 ; NotNull : true
        public boolean isGod;

        public Purchased(LiveMaybeListResp.Purchased purchased) {
            this.liveCourseId = purchased.liveCourseId;
            this.subject = purchased.subject;
            this.isGod = purchased.isGod;
            onConstructor();
        }

        public Purchased(LiveMaybeList1Resp.Purchased purchased) {
            this.liveCourseId = purchased.liveCourseId;
            this.subject = purchased.subject;
            this.isGod = purchased.isGod;
            onConstructor();
        }

        public Purchased(LiveCourseList2Resp.Purchased purchased) {
            this.liveCourseId = purchased.liveCourseId;
            this.subject = purchased.subject;
            this.isGod = purchased.isGod;
            onConstructor();
        }

        void onConstructor() {// do Something
        }
    }
}
