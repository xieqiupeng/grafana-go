//service层
import {MonitorTaskDao} from '../dao/MonitorTaskDao';
export class MonitorTaskService{

    static searchTask($http,taskName){
        var param='taskName='+taskName;
        MonitorTaskDao.searchTask($http,param);
    }
    static addTask($http){
        var param='taskName=testName';
        MonitorTaskDao.addTask($http,param);
    }
    //启动/暂停
    static startOrStopTask($http,id){
        MonitorTaskDao.startOrStopTask($http,id);
    }
    //编辑
    static editTask($http,id){
        MonitorTaskDao.editTask($http,id);
    }
    //删除
    static deleteTask($http,id){
        MonitorTaskDao.deleteTask($http,id);
    }


}




