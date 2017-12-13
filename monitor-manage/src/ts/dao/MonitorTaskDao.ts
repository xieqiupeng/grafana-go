//dao层
import {HttpUtils} from '../util/HttpUtils';
export class MonitorTaskDao{

    //搜索
    static searchTask($http,param){
        alert("dao:"+HttpUtils.invokeSimpleHttpGETRequestMethod($http,'monitortask/searchtaskbytaskname',param));
    }
    //新增
    static addTask($http,task){
        alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/addtask',task));
    }
    //启动/暂停
    static startOrStopTask($http,id){
        alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/startorstoptask',id));
    }
    //编辑
    static editTask($http,task){
        alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/edittask',task));
    }
    //删除
    static deleteTask($http,id){
        alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'monitortask/deletetask',id));
    }


}




