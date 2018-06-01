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

concurWrite = function() {
  for(i = 0; i < 10; i++)
  {
    //TODO not working properly
    //webPost(true, 0, "testFile", "user"+i, 0, i);
  }
}();
