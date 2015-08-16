$(function() { 

    //init();

  $("#hamburger").on("click",function(){
      toggleSidebar();
   })

   $("#dimmer").on("click",function(){
      if ($("body").hasClass("sidebar_visible")) {
         toggleSidebar();
      }
   })
    
  
  // Keep a mapping of url-to-container for caching purposes.
  var cache = {
    // If url is '' (no fragment), display this div's content.
    '': $('.bbq-default')
  };
    
    var thisNavItem;
  
  // Bind an event to window.onhashchange that, when the history state changes,
  // gets the url from the hash and displays either our cached content or fetches
  // new content to be displayed.
  $(window).bind( 'hashchange', function(e) {
    
    // Get the hash (fragment) as a string, with any leading # removed. Note that
    // in jQuery 1.4, you should use e.fragment instead of $.param.fragment().
    var url            = $.param.fragment();
      if (url === "") {url = "pages/introduction.html?&cat=concur&parent=concur&title=Introduction"};
    var myObj          = $.deparam(url);
    var currentHash    = url && $('a[href="#' + url + '"]');
    var thisParent     = currentHash.parent().parents("li");
    var thisCategory   = myObj.parent;
    var thisTitle      = myObj.title;
    var parentCategory = myObj.cat;
    
    // Remove .bbq-current class from any previously "current" link(s).
    $('a.bbq-current').removeClass('bbq-current');
    
    // Hide any visible ajax content.
    $('#main').children(':visible').hide();
    
    // Add .bbq-current class to "current" nav link(s), only if url isn't empty.
    url && $('a[href="#' + url + '"]').addClass('bbq-current');
      
      
    //click on the accordion menu to open it to the right place
      if (thisCategory === parentCategory) {
          thisNavItem = $(".accordion").find("[data-class='" + thisCategory + "']").find("span:first");

          //hide any other open menus
          thisNavItem.siblings().find("ul").slideUp(200);

          //close the sidebar if it's open
          toggleSidebar(close);
      }
      else {
          
          thisNavItem = $(".accordion").find("[data-class='" + parentCategory + "']").find("[data-class='" + thisCategory + "']").find("span:first");
          toggleSidebar(close);
      }
    
     
    /* colorize the nav ------------ */
    $("#main_nav").find(".active").removeClass("active");
    thisParent.addClass("active");
    currentHash.addClass("active");

    /* Set the Header ------------ */
    setHeader(thisTitle,thisCategory,parentCategory);
    
    if ( cache[ url ] ) {
      // Since the element is already in the cache, it doesn't need to be
      // created, so instead of creating it again, let's just show it!
      cache[ url ].show();
      
    } else {
      // Show "loading" content while AJAX content loads.
      $('.distractor').show();
      
      // Create container for this url's content and store a reference to it in
      // the cache.
      cache[ url ] = $('<div class="bbq-item"/>')
        
        // Append the content container to the parent container.
        .appendTo('#main')
        
        // Load external content via AJAX. Note that in order to keep this
        // example streamlined, only the content in .infobox is shown. You'll
        // want to change this based on your needs.
        .load( url, function(){
          // Content loaded, hide "loading" content.
          $( '.distractor' ).hide();
          //fade in the container
      $(".container").addClass("fadeIn");

      //fade in any elements that have the fadeInElemnt class
      fadeinElements();
        });
    }
  })
  
  // Since the event is only triggered when the hash changes, we need to trigger
  // the event now, to handle the hash the page may have loaded with.
  $(window).trigger('hashchange');
    accordionMenu(thisNavItem);
     
  
    
    

    
    $(".accordion span").on("click",function(){
        accordionMenu($(this));
    })
    
    



}); /* end document.ready ------------ */


function accordionMenu(inLink) {

   var currentLink  = inLink;
   var nextItem     = currentLink.next();
   var linkParent   = currentLink.parent();
   var linkSiblings = linkParent.siblings().find("ul");
   var linkTop      = currentLink.parentsUntil(".accordion").length;

   //check and see if this link is visible if it is, toggle it off
   if (nextItem.is(':visible')) {
      //currentLink.closest('li').removeClass('active');
      nextItem.slideUp(200);
      changeActive();
   }

   //this link isn't visible so let's open it and close the others
   else if(!nextItem.is(':visible')) {
      //this is a sub menu so open it
      if (linkTop > 1) {
         nextItem.parents("ul").css("display","block");
         nextItem.slideDown(200);

         linkSiblings.slideUp(200);

      }

      else {
         $("li.active ul").slideUp(200);
         nextItem.slideDown(200);
         changeActive();
      }
      
   }

   function changeActive() {
      $('.accordion').find("active").removeClass('active');
      currentLink.closest('li').addClass('active');
      //currentLink.parents("span").addClass("active");
   }

   if(currentLink.closest('li').find('ul').children().length == 0) {
      return true;
   } else {
      return false; 
   }

}





/* start all the initial load stuff ------------ */
function init() {
   var firstPage = $("#main_nav ul li ul a:first-child");
   var firstItem = $("li[data-class='concur']");

   firstItem.addClass("active");
   firstItem.find("ul").css("display","block");
   firstItem.find("ul > li:first-child a").addClass("active");

   /* load the url ------------ */
   //loadUrl(firstPage.attr("href"));
   setHeader(firstPage.html(),"concur","concur");

   if ($("#hamburger .icon").is(":visible")) {
      toggleSidebar();
   }

   //$("#main_nav a:first-child").click();

}





/* add the title to the header when a new page is loaded ------------ */
function setHeader(inTitle,inCategory,topCategory) {

   var title = inTitle;
   var category = "";
   if (inCategory == topCategory) {
      category = topCategory;
   }
   else {
      category = topCategory + "  <span><svg class='breadcrumb_arrow_position' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' width='25px' height='25px' viewBox='0 0 30 30' style='enable-background:new 0 0 30 30;' xml:space='preserve'><polygon fill='#FFFFFF' class='breadcrumb_arrow' points='7.5,0 22.5,15 7.5,30 15.6,15'/></svg></span>  " + inCategory;
   }


   title = category + "  <span><svg class='breadcrumb_arrow_position' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' width='25px' height='25px' viewBox='0 0 30 30' style='enable-background:new 0 0 30 30;' xml:space='preserve'><polygon fill='#FFFFFF' class='breadcrumb_arrow' points='7.5,0 22.5,15 7.5,30 15.6,15'/></svg></span>  " + inTitle;

   //title = category + "  <span><img id='breadcrumb_arrow' src='img/icons_web/breadcrumb_arrow.svg' height='30' width='30'></span>  " + inTitle;
   // title = category + "  <span>&rsaquo;</span>  " + inTitle;

   $("#header_title").html(title);
   $("#breadcrumb_header_title").html(title);
   $("#body").removeClass().addClass(topCategory);
}  




// get the commit notes from github so there's a changelist on the site
function getGithubCommits() {
  var html = "<thead><tr><th class='td_20'>Date</th><th class='td_60'>Description</th></tr></thead><tbody>";
  //  var html = "<ul>";
  $.getJSON("https://api.github.com/repos/haymaker17/mobile_labs/commits", function(data){
    $.each(data, function(key, val){
      var item = data[key];
      var message = item.commit.message;
      
      //format date
      var formattedDate = new Date(item.commit.author.date);
      var d = formattedDate.getDate();
      var m =  formattedDate.getMonth();
      m += 1;  // JavaScript months are 0-11
      var y = formattedDate.getFullYear();

      var date = (m + "." + d + "." + y);


	  html+= "<tr><td>" + date + "</td><td>" + message + "</td></tr>";
      // html+= "<li>" + date + "<br><span>" + message + "</span></li>";
    });

    html += "</tbody>"
    // html += "</ul>"

    $(".changelist").html(html);
  });

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



$(window).scroll(function () {

     fadeinElements();
     fixHeader();

 });





 function fadeinElements() {
     /* Check the location of each desired element */
     $('.fadeinElement').each(function(i) {

         var height_of_object = $(this).outerHeight();
         var bottom_of_object = $(this).offset().top + height_of_object;
         var middle_of_object = bottom_of_object - (height_of_object);
         var bottom_of_window = $(window).scrollTop() + $(window).height();

         /* If the object is completely visible in the window, fade it it */
         if (bottom_of_window > middle_of_object) {

             // $(this).animate({
             //     'opacity': '1'
             // }, 500);

             $(this).addClass("visible");

         }

     });
 }



function fixHeader() {

   var fromTop   = $("body").scrollTop();
   $('body').toggleClass("fixedHeader", (fromTop > 60));
   
}




function countItems() {

   var itemCount = {
      all: 0,
      filter_list: 0,
      filter_content: 0,
      filter_bars: 0,
      filter_controls: 0,
      filter_login: 0,
      filter_temp_view: 0,
      filter_status: 0
   };

   $('.filterItemsContainer > div').each(function(index, element){
     var thisClass  = $(element).attr("class");
     itemCount[thisClass] += 1;
     itemCount.all += 1;
     
   });

   $('.filterOptions li').each(function(index, element){
     var thisId   = $(element).data("filter");
     var thisSpan = $(element).find("span");

     thisSpan.html(itemCount[thisId]);
     
   });
}




function selectFirstItem() {
   
   if ($(".filterOptions").length > 0) {
      $(".filterOptions li:first-child").click();
   }
}





