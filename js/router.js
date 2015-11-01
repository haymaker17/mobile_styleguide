var app = angular.module('mobileStyleguide', [
	'ngRoute',
	'ngAnimate'
]);

//when you click on the nav, load the partials into the ng-view
//also added a controller so we can run code when the partials are loaded. See the scripts.js file for that funciton.
app.config(function($routeProvider) {
	//set routes
    $routeProvider
        .when('/', {
            templateUrl: 'pages/introduction.html'
        })
        .when('/pages/:name*', {
        	templateUrl: function(urlattr){
        		return 'pages/' + urlattr.name + '.html';
        	},
            controller: 'routeController'
        })
        .otherwise({
        	redirectTo: 'pages/introduction.html'
      	});
        
});
