//service层
import {MonitorTaskDao} from '../dao/MonitorTaskDao';
export class MonitorTaskService{

    static searchTask($http){
        MonitorTaskDao.searchTask($http);
    }
    static addTask($http){
        MonitorTaskDao.addTask($http);
    }

}




