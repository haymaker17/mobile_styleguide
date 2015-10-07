$(function() { 

  //initialize scroll reveal js
  window.sr = new scrollReveal();

  //make the hamburger menu work
  $("#hamburger").on("click",function(){
      toggleSidebar();
   })

  //click on the dimmer to close the menu
   $("#dimmer").on("click",function(){
      if ($("body").hasClass("sidebar_visible")) {
         toggleSidebar();
      }
   })
  
  //fire the accordion
  $(".accordion").on("click","a, span", function(event){
        accordionMenu($(this),event);
        $("#main").scrollTop(0);
    })

  //onload, get the current page and make the left navigation and breadcrumbs work
  if (window.location.hash){
      //get the url - if it's blank, start with the intro page
      var hash = window.location.hash.substring(2).length ? window.location.hash.substring(2) : "pages/introduction";
      var currentHash = $('a[href="#' + hash + '"]');
      var parent_of_hash = currentHash.parents("ul").siblings("span");

      //trigger a click on the sidenav
      currentHash.trigger("click");
      parent_of_hash.trigger("click");
      currentHash.parent("li").addClass('active');

   }
   

}); /* end document.ready ------------ */


//run when partials are loaded
function routeChangeScripts(inRoute) {

  //sr.init();

}



function accordionMenu(inLink,inEvent) {

    var currentLink    = inLink;
    var currentNode    = currentLink.prop("tagName");
    var nextItem       = currentLink.next();
    var linkParent     = currentLink.parent();
    var linkSiblings   = linkParent.siblings().find("ul");
    var linkTop        = currentLink.parentsUntil(".accordion").length;

    //if this is a link get it's data attributes
    var thisCategory   = currentLink.data("parent") ? currentLink.data("parent") : "" ;
    var parentCategory = currentLink.data("cat") ? currentLink.data("cat") : "";
    var thisTitle      = currentLink.text();
    
    //This is a link so change the header
    if (currentNode == "A") {
      setHeader(thisTitle,thisCategory,parentCategory);
      toggleSidebar(close);
    }

   //check and see if this link's children are already visible.  if they are, toggle it off
   if (nextItem.is(':visible')) {
      nextItem.slideUp(200);
   }

   //this link's children aren't visible so let's open it and close the others
   else if(!nextItem.is(':visible')) {
      
      //this is a sub menu so open it
      if (linkTop > 1) {
         nextItem.parents("ul").css("display","block");
      }

      //this is the top level menu so open it up
      else {
         $("li.active ul").slideUp(200);
      }

      nextItem.slideDown(200);
      linkSiblings.slideUp(200);

   }

   changeActive();

   function changeActive() {
      $('.accordion').find(".active").removeClass('active');
      currentLink.closest('li').addClass('active');
      currentLink.parents("ul").siblings("span").addClass("active");
   }



   if(currentLink.closest('li').find('ul').children().length == 0) {
      return true;
   } else {
      return false; 
   }

}



/* add the title to the header when a new page is loaded ------------ */
function setHeader(inTitle,inCategory,topCategory) {

   var title = inTitle;
   var category = "";

   //check and see if we're already on the top category
   if (inCategory == topCategory) {
      category = topCategory;
   }
   else {
      category = topCategory + "  <span><svg class='breadcrumb_arrow_position' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' width='25px' height='25px' viewBox='0 0 30 30' style='enable-background:new 0 0 30 30;' xml:space='preserve'><polygon fill='#FFFFFF' class='breadcrumb_arrow' points='7.5,0 22.5,15 7.5,30 15.6,15'/></svg></span>  " + inCategory;
   }

   title = category + "  <span><svg class='breadcrumb_arrow_position' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' width='25px' height='25px' viewBox='0 0 30 30' style='enable-background:new 0 0 30 30;' xml:space='preserve'><polygon fill='#FFFFFF' class='breadcrumb_arrow' points='7.5,0 22.5,15 7.5,30 15.6,15'/></svg></span>  " + inTitle;

   $("#header_title").html(title);
   $("#breadcrumb_header_title").html(title);
   $("#body").removeClass().addClass(topCategory);
}  




// get the commit notes from github so there's a changelist on the site
function doChangelist(response) {
	var meta = response.meta;
	var data = response.data;
	
	var older = meta.Link[0];
	var first = meta.Link[1];
	var newer = meta.Link[2];

    outputChangelist(data,older,first,newer)

}


//loop through each changelist item and build the table. also build the pagination. 
function outputChangelist(inData,inOlder,inFirst,inNewer) {
	
	var html = "<thead><tr><th class='td_20'>Date</th><th class='td_60'>Description</th></tr></thead><tbody>";
	var links = "<div class='clangelist_nav'>";
	
	//for pagination
	if (inOlder != undefined) {
		links += "<a href='#' data-link='" + inOlder[0] + "' class='float_left'>older changes</a>";
	}
/*
	if (inFirst != undefined) {
		links += "<a href='#' data-link='" + inFirst[0] + "' class='centered'>latest checkins</a>";
	}
*/
    if (inNewer != undefined) {
	    links += "<a href='#' data-link='" + inNewer[0] + "' class='float_right'>newer changes</a>";
    }
    
    links += "";
    
	
	//loop through the data and spit out the html for the changes
	$.each(inData, function(key, val){
      var item    = inData[key];
      var message = item.commit.message;
      var url     = item.html_url;
      
      //format date
      var formattedDate = new Date(item.commit.author.date);
      var d = formattedDate.getDate();
      var m = formattedDate.getMonth();
      m += 1;  // JavaScript months are 0-11
      var y = formattedDate.getFullYear();

      var date = (m + "." + d + "." + y);

      //throwing out the merge branch changes
      if (message.substring(0,12) != 'Merge branch' ) {

        html+= "<tr><td>" + date + "</td><td><a href='" + url + "' target='_blank'>" + message + "</a></td></tr>";
      }
	  
    });


	//append html
    html += "</tbody>";
    $(".changelist").html(html);
    $(".table_nav").html(links)
    
    //function to re-fire the script when a link is clicked on
    $(".clangelist_nav").on('click', "a", function(ev){
	    ev.preventDefault();
	    
		var newPage = $(this).data("link");
		createChangeListScript(newPage);
		
	});
}



// create the script to add the changelist. This is so we can get the link header files for pagination
function createChangeListScript(inLink) {
	
	var script = document.createElement('script');
	script.src = inLink;
	
	document.getElementsByTagName('head')[0].appendChild(script);

}




/* open and close the sidebar when clicked ------------ */
function toggleSidebar(inState) {
   var body = $("body");

   if (inState == close) {
      body.removeClass("sidebar_visible");
   }

   else {
      if (body.hasClass("sidebar_visible")) {
        body.removeClass("sidebar_visible");
     }
     else {
        body.addClass("sidebar_visible");
     }

   }
}



//scrolly stuff
$(window).scroll(function () {
     fixHeader();
 });



function fixHeader() {

   var fromTop   = $("body").scrollTop();
   $('body').toggleClass("fixedHeader", (fromTop > 60));
   
}
