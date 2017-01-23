cordova.define("shengdeng.jingu.payment.payment", function(require, exports, module) {

var exec = require("cordova/exec");

function Payment(){};

Payment.prototype.auth = function(params){
    exec(null,null,"Wechat","auth",[]);
}

Payment.prototype.pay = function(params,success){
    exec(null,null,"Wechat","pay",[params]);
}

var payment = new Payment();

module.exports = payment;
});


//               var exec = require('cordova/exec');
//
//               var Wechat = {
//
//               auth: function(params) {
//               return exec(params, null, 'Wechat', 'auth', []);
//               }
//
//               };
//
//               module.exports = Wechat;