// //web服务端地址
// var serverUrl='http://localhost:8075/';
//
// //Http工具类
// export class HttpUtils {
//
//     static invokeSimpleHttpPOSTRequestMethodByJSON($http,contextPath,param) {
//         console.log("invokeHttpPOSTRequestMethod param:",param);
//
//         $http({
//             url: serverUrl+contextPath,
//             method: 'POST',
//             data: JSON.stringify({"taskName":"标题","dataSourceIp":"10.100.1.67"}),
//         }).then((rsp) => {
//             console.log("invoke invokeHttpPOSTRequestMethod ok:", rsp);
//             alert('rsp.data:'+rsp.data);
//             return JSON.stringify(rsp.data);
//         }, err => {
//             console.log("invoke invokeHttpPOSTRequestMethod error:", err);
//             return err;
//         });
//     }
//
//
//     static invokeSimpleHttpPOSTRequestMethod($http,contextPath,param) {
//         console.log("invokeHttpPOSTRequestMethod param:",param);
//
//         $http({
//             url: serverUrl+contextPath+"1",
//             method: 'POST',
//             headers: {
//                 "Content-Type": "application/x-www-form-urlencoded"
//             },
//             data: param,
//         }).then((rsp) => {
//             console.log("invoke invokeHttpPOSTRequestMethod ok:", rsp);
//             alert('rsp.data:'+rsp.data);
//             return JSON.stringify(rsp.data);
//         }, err => {
//             console.log("invoke invokeHttpPOSTRequestMethod error:", err);
//             return err;
//         });
//     }
//
//     static invokeSimpleHttpGETRequestMethod($http,contextPath,param,taskArray) {
//         console.log("invokeHttpGETRequestMethod param:"+param);
//         $http({
//             url: serverUrl+contextPath+"?"+param,
//             method: 'GET'
//         }).then((rsp) => {
//             console.log("invoke invokeHttpGETRequestMethod ok:", rsp.data);
//         }, err => {
//             console.log("invoke invokeHttpGETRequestMethod err:", err);
//         });
//     }
//
// }
//
//
//
//
