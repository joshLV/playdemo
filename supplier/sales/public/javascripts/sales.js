$(document).ready(function(){
    $("#district").change(function(){
    	var areaId= $("#district").val();
    	var url="@{Shops.relation()}";
    	 $.ajax({
             url: url,
             data:"areaId="+areaId,
             type: 'GET',
             dataType: 'JSON',
             error: function() { alert('Error loading data!'); },
             success: function(msg) {
                 $("#area").empty();
                 $.each(eval(msg), function(i, item) {
                     $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#area"));
                 });
             }
         });
    });
    $("#area").change();
}); 