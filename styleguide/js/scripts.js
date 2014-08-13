$(function() { 

   init();


   //when you click on a link in the sidebar nav, load the page in the "main" div
   $("#main_nav").on("click","a",function(ev){
      ev.preventDefault();

      var currentLink  = $(this);
      var thisParent   = currentLink.parent().parents("li");
      var thisCategory = thisParent.data("class");
      var parentCategory = currentLink.parents(".accordion").children("li.active").data("class");
      var nextElement  = currentLink.next();
      var thisUrl      = $(this).attr("href");
      var thisTitle    = $(this).html();

      /* lets see if this has children and needs to drop down first ------------ */
      if(nextElement.is('ul')){
         accordionMenu(currentLink);
      }

      /* this doesn't have a submenu so just get the link ------------ */
      else {

         /* colorize the nav ------------ */
         $("#main_nav").find(".active").removeClass("active");
         thisParent.addClass("active");
         currentLink.addClass("active");

         /* load the url ------------ */
         loadUrl(thisUrl);
         setHeader(thisTitle,thisCategory,parentCategory);

         if ($("#hamburger .icon").is(":visible")) {
            toggleSidebar();
         }
 
         //if a sub nav is showing, close it.
         currentLink.parent("li").siblings('li').find('ul').slideUp();

      }
   })


  



}); /* end document.ready ------------ */


function accordionMenu(inLink) {

   var currentLink = inLink;
   var nextItem    = inLink.next();
   var linkParent  = inLink.parentsUntil(".accordion").length;

   //check and see if this link is visible if it is, toggle it off
   if (nextItem.is(':visible')) {
      //currentLink.closest('li').removeClass('active');
      nextItem.slideUp(200);
      changeActive();
   }

   //this link isn't visible so let's open it and close the others
   else if(!nextItem.is(':visible')) {
      //this is a sub menu so open it
      if (linkParent > 1) {
         nextItem.slideDown(200);
      }

      else {
         $("li.active ul").slideUp(200);
         nextItem.slideDown(200);
         changeActive();
      }
      
   }

   function changeActive() {
      $('.accordion li').removeClass('active');
      currentLink.closest('li').addClass('active');
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
   loadUrl(firstPage.attr("href"));
   setHeader(firstPage.html(),"concur","concur");

   if ($("#hamburger .icon").is(":visible")) {
      toggleSidebar();
   }

   $("#main_nav a:first-child").click();

}


//do the ajax load into the #main div
function loadUrl(inLink) {

   $("#main").load(inLink,function(){
      
      //make sure the window always starts at the top
      $(window).scrollTop(0);

      //fade in the container
      $(".container").addClass("fadeIn");

      //fade in any elements that have the fadeInElemnt class
      fadeinElements();

      //the filtering mechanism
      $('.filterOptions').on("click", "li", function() {
         sortItems($(this));
      });

      // // init Isotope
      // var $container = $('#isotope').isotope({
      //   // options
      // });


      // // filter items on button click
      // $('#filters').on( 'click', 'li', function() {
      //   var filterValue = $(this).attr('data-filter');
      //   $container.isotope({ filter: filterValue });
      // });


      countItems();
      selectFirstItem();
   });

}



/* add the title to the header when a new page is loaded ------------ */
function setHeader(inTitle,inCategory,topCategory) {

   var title = inTitle;
   var category = "";
   if (inCategory == topCategory) {
      category = topCategory;
   }
   else {
      category = topCategory + "  >  " + inCategory;
   }

   title = category + "  >  " + inTitle;

   $("#header_title").html(title);
   $("#body").removeClass().addClass(topCategory);
}  


