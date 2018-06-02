_init = function() {
  for(i = 0; i < 10; i++)
  {
    userName = "user"+i;
    $.ajax({
        url: "/users?user="+userName,
        type: "POST",
        data: userName,
    });
  }
  fileName = "testFile";
  $.ajax({
      url: "/documents?name="+fileName,
      type: "POST",
      data: fileName,
  });
}

init = function() {
  succ = false;
  $.ajax({
      url: "documents/d?documentId="+"testFile",
      type: "GET",
      error: _init
  });
}();


//periodically insert
sessionStorage.setItem("count", 0);

var id = 0;

insertTest = function() {
  count = sessionStorage.getItem("count");
  console.log("count: " + count);
  webPost(true, 0, count);
  count = parseInt(count)+1;
  if(count == 10)
  {
    clearInterval(id);
  }
  sessionStorage.setItem("count", count);
};

periodTest = function() {
  id = setInterval(insertTest, 1000);
};

$("#test").click(periodTest);
