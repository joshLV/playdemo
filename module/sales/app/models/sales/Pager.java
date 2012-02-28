package models.sales;


public class Pager {
	public int pageSize=15;//每页记录数
	public long totalCount=0;//总记录数
	public long totalpage=0;//总页数
	public int currPage=1;//当前页码
	public int startNum = 0;
	public int endNum = 0;
	public int nextPage = 0;
	public int prevPage = 0;

	public void totalPager(){

		if(currPage < 1){
			currPage = 1;
		}

		totalpage = totalCount/pageSize;
		if(totalCount%pageSize !=0) {
			totalpage++;
		}

		if(totalpage<=0){
			totalpage = 1;
		}

		prevPage = currPage > 1 ?  currPage - 1:1;      
		nextPage = currPage < totalpage ? currPage + 1:Integer.parseInt(String.valueOf(totalpage));  

		if(currPage == totalpage){
			startNum = currPage - pageSize/2 -1;
		}else{
			startNum = currPage - pageSize/2;
		}

		startNum = startNum >=1 ?startNum:1;
		endNum = startNum + pageSize -1;
		if(endNum > totalpage){
			endNum = Integer.parseInt(String.valueOf(totalpage));
		}

		if(endNum <=0){
			endNum = 1;
		}

	}

}
