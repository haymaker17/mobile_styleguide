$(function() { 


   $("#hamburger .icon").on("click",function(){
      toggleSidebar();
   })

   $("#dimmer").on("click",function(){
      if ($("body").hasClass("sidebar_visible")) {
         toggleSidebar();
      }
   })




}); /* end document.ready ------------ */


/* open and close the sidebar when clicked ------------ */
function toggleSidebar() {
   var body = $("body");

   if (body.hasClass("sidebar_visible")) {
      body.removeClass("sidebar_visible");
   }
   else {
      body.addClass("sidebar_visible");
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



function sortItems(inThis) {

   var thisOne = inThis;

   // fetch the id of the clicked item
   var ourId = thisOne.data('filter');

   // reset the active class on all the buttons
   $('.filterOptions li').removeClass('active');
   // update the active state on our clicked button
   thisOne.addClass('active');

   if(ourId == 'all') {
      // show all our items
      $('.filterItemsContainer').children('li').removeClass("hidden");
   }
   else {
      // hide all elements that don't share ourId
      $('.filterItemsContainer').children('li:not(.' + ourId + ')').addClass('hidden');
      // show all elements that do share ourId
      $('.filterItemsContainer').children('li.' + ourId).removeClass("hidden");
   }
   return false;
}

function countItems() {

   var itemCount = {
      all: 0,
      list: 0,
      content: 0,
      bars: 0,
      controls: 0,
      login: 0,
      temp_view: 0,
      states: 0
   };

   $('.filterItemsContainer li').each(function(index, element){
     var thisClass  = $(element).attr("class");
     itemCount[thisClass] += 1;
     itemCount.all += 1;
     
   });

   $('.filterOptions li').each(function(index, element){
     var thisId  = $(element).data("filter");
     var thisSpan = $(element).find("span");

     thisSpan.html(itemCount[thisId]);
     
   });
}

function selectFirstItem() {
   
   if ($(".filterOptions").length > 0) {
      $(".filterOptions li:first-child").click();
   }
}
