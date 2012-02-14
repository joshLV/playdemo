package models.sales;

import java.util.List;
import java.util.Map;

import play.mvc.Scope.Params;

public class Pager {

    public String pageName="page";//页码名称
    public int pageSize=15;//每页记录数
    public long totalCount=0;//总记录数
    public long totalpage=0;//总页数
    public int currPage=1;//当前页码
    public int numsize = 10;
    public Map<String, String> params;
    
    public int startNum = 0;
    public int endNum = 0;
    public int nextPage = 0;
    public int prevPage = 0;
    
    
    public String param_url = "";
    
    public List list;//查询结果
    
    
    public void totalPager(){
        
        if(pageSize < 1){
            pageSize = 10;
        }
        
        if(currPage < 1){
            currPage = 1;
        }
        
        if(numsize < 1){
            numsize = 10;
        }
        totalpage = totalCount/pageSize;
        
        if(totalpage<=0){
            totalpage = 1;
        }
        
        
        if(currPage > totalpage){
            currPage =  Integer.parseInt(String.valueOf(totalpage));
        }
        
        
        prevPage = currPage > 1 ?  currPage - 1:1;      
        nextPage = currPage < totalpage ? currPage + 1:Integer.parseInt(String.valueOf(totalpage));  

        if(currPage == totalpage){
            startNum = currPage - numsize/2 -1;
        }else{
            startNum = currPage - numsize/2;
        }
        startNum = startNum >=1 ?startNum:1;
        endNum = startNum + numsize -1;
        if(endNum > totalpage){
            endNum = Integer.parseInt(String.valueOf(totalpage));
        }
        
        if(endNum <=0){
            endNum = 1;
        }
        
        
//        foreach ($param as $key => $val){
//            $param_url .= $key.'='.$val.'&';
//        }
        
    }
    
}
