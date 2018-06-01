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
  if(succ)
    return;
  console.log("here");
}();

concurWrite = function() {

}();
