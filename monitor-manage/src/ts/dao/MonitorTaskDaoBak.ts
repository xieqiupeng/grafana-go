// //dao层
//
// //web服务端地址
// var serverUrl='http://localhost:8075/';
//
// export class MonitorTaskDao{
//
//     //搜索
//     static searchTask($http,param,taskArray){
//         $http({
//             url: serverUrl+'monitortask/searchtaskbytaskname'+"?"+param,
//             method: 'GET'
//         }).then((rsp) => {
//             console.log("invoke invokeHttpGETRequestMethod ok:", rsp.data);
//             taskArray=rsp.data;
//             alert(taskArray);
//         }, err => {
//             console.log("invoke invokeHttpGETRequestMethod err:", err);
//         });
//     }
//
//     // //新增
//     // static addTask($http,task){
//     //     alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/addtask',task));
//     // }
//     // //启动/暂停
//     // static startOrStopTask($http,id){
//     //     alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/startorstoptask',id));
//     // }
//     // //编辑
//     // static editTask($http,task){
//     //     alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/edittask',task));
//     // }
//     // //删除
//     // static deleteTask($http,id){
//     //     alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/deletetask',id));
//     // }
//
//
// }
//
//
//
//
