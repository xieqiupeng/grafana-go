//dao层
import {HttpUtils} from '../util/HttpUtils';
export class MonitorTaskDao{


    static searchTask($http){
        alert("dao:"+HttpUtils.invokeSimpleHttpGETRequestMethod($http,'test/searchTask','{"data"," post"}'));
    }
    static addTask($http){
        alert("dao:"+HttpUtils.invokeSimpleHttpPOSTRequestMethod($http,'test/addTask','{"data"," post"}'));
    }

}




