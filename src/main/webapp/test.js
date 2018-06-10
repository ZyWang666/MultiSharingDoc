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

//test1: insert a number per sec at pos 0
insertTest = function() {
  count = sessionStorage.getItem("count");
  console.log("count: " + count);
  webPost(true, 1, count);
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

$("#test1").click(periodTest);

/*
//test2: insert a number per sec at pos 9-0
insertTest = function(i) {
  count = sessionStorage.getItem("count");
  console.log("count: " + count);
  webPost(true, 9-count, count);
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

$("#test2").click(periodTest);

//test3: insert-delete pattern test
insertTest = function(i) {
  count = sessionStorage.getItem("count");
  console.log("count: " + count);
  var payload;
  if(count % 2 == 0)
    payload = count;
  else
    payload = "Backspace";
  webPost(true, 1, payload);
  count = parseInt(count)+1;
  if(count == 10)
  {
    clearInterval(id);
  }
  sessionStorage.setItem("count", count);
};

$("#test3").click(periodTest);
*/
