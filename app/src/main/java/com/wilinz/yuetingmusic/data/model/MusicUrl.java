package com.wilinz.yuetingmusic.data.model;

import java.util.List;

public class MusicUrl{

    /**
     * data : [{"id":33894312,"url":"http://m7.music.126.net/20221213011458/0e8e9a75c0b99248ad55dd26db1e7c5e/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3","br":320000,"size":10691439,"md5":"a8772889f38dfcb91c04da915b301617","code":200,"expi":1200,"type":"mp3","gain":-6.3072,"peak":1,"fee":0,"uf":null,"payed":0,"flag":1,"canExtend":false,"freeTrialInfo":null,"level":"exhigh","encodeType":"mp3","freeTrialPrivilege":{"resConsumable":false,"userConsumable":false,"listenType":null},"freeTimeTrialPrivilege":{"resConsumable":false,"userConsumable":false,"type":0,"remalongime":0},"urlSource":0,"rightSource":0,"podcastCtrp":null,"effectTypes":null,"time":267232}]
     * code : 200
     */

    public long code;
    public List<MusicInfo> data;
    
    public static class MusicInfo {
        /**
         * id : 33894312
         * url : http://m7.music.126.net/20221213011458/0e8e9a75c0b99248ad55dd26db1e7c5e/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3
         * br : 320000
         * size : 10691439
         * md5 : a8772889f38dfcb91c04da915b301617
         * code : 200
         * expi : 1200
         * type : mp3
         * gain : -6.3072
         * peak : 1
         * fee : 0
         * uf : null
         * payed : 0
         * flag : 1
         * canExtend : false
         * freeTrialInfo : null
         * level : exhigh
         * encodeType : mp3
         * freeTrialPrivilege : {"resConsumable":false,"userConsumable":false,"listenType":null}
         * freeTimeTrialPrivilege : {"resConsumable":false,"userConsumable":false,"type":0,"remalongime":0}
         * urlSource : 0
         * rightSource : 0
         * podcastCtrp : null
         * effectTypes : null
         * time : 267232
         */

        public long id;
        public String url;
        public long br;
        public long size;
        public String md5;
        public long code;
        public long expi;
        public String type;
        public double gain;
        public double peak;
        public long fee;
        public Object uf;
        public long payed;
        public long flag;
        public boolean canExtend;
        public Object freeTrialInfo;
        public String level;
        public String encodeType;
        public FreeTrialPrivilegeBean freeTrialPrivilege;
        public FreeTimeTrialPrivilegeBean freeTimeTrialPrivilege;
        public long urlSource;
        public long rightSource;
        public Object podcastCtrp;
        public Object effectTypes;
        public long time;

        public static class FreeTrialPrivilegeBean  {
            /**
             * resConsumable : false
             * userConsumable : false
             * listenType : null
             */

            public boolean resConsumable;
            public boolean userConsumable;
            public Object listenType;
        }
        
        public static class FreeTimeTrialPrivilegeBean{
            /**
             * resConsumable : false
             * userConsumable : false
             * type : 0
             * remalongime : 0
             */

            public boolean resConsumable;
            public boolean userConsumable;
            public long type;
            public long remalongime;
        }
    }
}
