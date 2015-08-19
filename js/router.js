var app = angular.module('mobileStyleguide', [
	'ngRoute',
	'ngAnimate'
]);

//when you click on the nav, load the partials into the ng-view
app.config(function($routeProvider) {
	//set routes
    $routeProvider
        .when('/', {
            templateUrl: 'pages/introduction.html'
        })
        .when('/pages/:name*', {
        	templateUrl: function(urlattr){
        		return 'pages/' + urlattr.name + '.html';
        	}
        })
        .otherwise({
        	redirectTo: 'pages/introduction.html'
      	});
        
});


app.run(function($rootScope, $location){
$rootScope.$on('$routeChangeStart', function(event, route){
    routeChangeScripts(route);
  });
});


