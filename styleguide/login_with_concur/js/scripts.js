$(function() {	

	var loaded = "login";
	var loader = $(".loader");

    loader.on("click","button", function(ev){
    	ev.preventDefault();

    	if (loaded === "login") {
    		loader.load("confirmation.html");
    		loaded = "confirmation";
   		}
   		else {
   			loader.load("login.html");
   			loaded = "login";
   		}
   		
   });

    loader.load("login.html");

});

