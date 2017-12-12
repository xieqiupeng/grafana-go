//web服务端地址
var serverUrl='http://localhost:8080/';

//Http工具类
export class HttpUtils {


    static invokeSimpleHttpPOSTRequestMethod($http,contextPath,param) {
        console.log("invokeHttpPOSTRequestMethod param:",param);

        $http({
            url: serverUrl+contextPath,
            method: 'POST',
            data: param
            // data: '{"data"," post"}'
            //,
            // headers: {
            //     "Content-Type": "plain/text"
            // }
        }).then((rsp) => {
            console.log("invoke invokeHttpPOSTRequestMethod ok:", rsp);
            return rsp;
        }, err => {
            console.log("invoke invokeHttpPOSTRequestMethod error:", err);
            return err;
        });
    }

    static invokeSimpleHttpGETRequestMethod($http,contextPath,param) {
        console.log("invokeHttpGETRequestMethod param:"+param);
        $http({
            url: serverUrl+contextPath,
            method: 'GET',
            data: param
            // data: '{"data"," get"}'
            //,
            // headers: {
            //     "Content-Type": "plain/text"
            // }
        }).then((rsp) => {
            console.log("invoke invokeHttpGETRequestMethod ok:", rsp);
            return rsp;
        }, err => {
            console.log("invoke invokeHttpGETRequestMethod ok:", err);
            return err;
        });
    }

}




