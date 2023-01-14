package com.wilinz.yuetingmusic.data.model

class MusicUrl {
    /**
     * data : [{"id":33894312,"url":"http://m7.music.126.net/20221213011458/0e8e9a75c0b99248ad55dd26db1e7c5e/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3","br":320000,"size":10691439,"md5":"a8772889f38dfcb91c04da915b301617","code":200,"expi":1200,"type":"mp3","gain":-6.3072,"peak":1,"fee":0,"uf":null,"payed":0,"flag":1,"canExtend":false,"freeTrialInfo":null,"level":"exhigh","encodeType":"mp3","freeTrialPrivilege":{"resConsumable":false,"userConsumable":false,"listenType":null},"freeTimeTrialPrivilege":{"resConsumable":false,"userConsumable":false,"type":0,"remalongime":0},"urlSource":0,"rightSource":0,"podcastCtrp":null,"effectTypes":null,"time":267232}]
     * code : 200
     */
    var code: Long = 0
    var data: List<MusicInfo>? = null

    class MusicInfo {
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
        var id: Long = 0
        var url: String? = null
        var br: Long = 0
        var size: Long = 0
        var md5: String? = null
        var code: Long = 0
        var expi: Long = 0
        var type: String? = null
        var gain = 0.0
        var peak = 0.0
        var fee: Long = 0
        var uf: Any? = null
        var payed: Long = 0
        var flag: Long = 0
        var canExtend = false
        var freeTrialInfo: Any? = null
        var level: String? = null
        var encodeType: String? = null
        var freeTrialPrivilege: FreeTrialPrivilegeBean? = null
        var freeTimeTrialPrivilege: FreeTimeTrialPrivilegeBean? = null
        var urlSource: Long = 0
        var rightSource: Long = 0
        var podcastCtrp: Any? = null
        var effectTypes: Any? = null
        var time: Long = 0

        class FreeTrialPrivilegeBean {
            /**
             * resConsumable : false
             * userConsumable : false
             * listenType : null
             */
            var resConsumable = false
            var userConsumable = false
            var listenType: Any? = null
        }

        class FreeTimeTrialPrivilegeBean {
            /**
             * resConsumable : false
             * userConsumable : false
             * type : 0
             * remalongime : 0
             */
            var resConsumable = false
            var userConsumable = false
            var type: Long = 0
            var remalongime: Long = 0
        }
    }
}