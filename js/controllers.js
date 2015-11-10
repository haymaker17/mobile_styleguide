/*
app.controller('MarketingFilter', ['$scope', function ($scope) { 
    $scope.screenshots = [
*/

app.controller('iOSMarketingFilter', function ($scope) { 
    $scope.screenshots = [
        {
        	url: 'img/marketing/ios/01_login.png', 
        	title: 'Sign In | Test Drive', 
        	category: 'Login',
        	platform: 'iOS'
        	
        },
        {
        	url: 'img/marketing/ios/02_main_menu.png', 
        	title: 'Main Menu', 
        	category: 'Login',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/03_trip.png', 
        	title: 'New Trip', 
        	category: 'Login',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/04_trip_options.png', 
        	title: 'Trip Options', 
        	category: 'Login',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/05_air_search.png', 
        	title: 'Air Search', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/06_air_results.png', 
        	title: 'Air Results', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/07_air_carrier.png', 
        	title: 'Air Carrier', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/08_air_details.png', 
        	title: 'Air Details', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/09_hotel_search.png', 
        	title: 'Hotel Search', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/10_hotel_results.png', 
        	title: 'Hotel Results', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/11_hotel_details.png', 
        	title: 'Hotel Details', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/12_hotel_photos.png', 
        	title: 'Hotel Photos', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/13_car_search.png', 
        	title: 'Car Search', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/14_car_results.png', 
        	title: 'Car Results', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/15_car_sort.png', 
        	title: 'Car Sort', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/16_car_details.png', 
        	title: 'Car Details', 
        	category: 'Travel',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/17_receipt.png', 
        	title: 'Receipt', 
        	category: 'Expense',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/18_expense.png', 
        	title: 'Expense', 
        	category: 'Expense',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/19_expense_list.png', 
        	title: 'Expense List', 
        	category: 'Expense',
        	platform: 'iOS'
        },
        {
        	url: 'img/marketing/ios/20_reports.png', 
        	title: 'Reports', 
        	category: 'Expense',
        	platform: 'iOS'
        }        
    ];
});


app.controller('AndrioidMarketingFilter', function ($scope) { 
    $scope.screenshots = [
		{
        	url: 'img/marketing/android/01_login.png', 
        	title: 'Sign In | Test Drive', 
        	category: 'Login',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/02_main_menu.png', 
        	title: 'Main Menu', 
        	category: 'Login',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/03_trip.png', 
        	title: 'New Trip', 
        	category: 'Login',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/04_trip_options.png', 
        	title: 'Trip Options', 
        	category: 'Login',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/05_air_search.png', 
        	title: 'Air Search', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/06_air_results.png', 
        	title: 'Air Results', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/07_air_carrier.png', 
        	title: 'Air Carrier', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/08_air_details.png', 
        	title: 'Air Details', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/09_hotel_search.png', 
        	title: 'Hotel Search', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/10_hotel_results.png', 
        	title: 'Hotel Results', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/11_hotel_details.png', 
        	title: 'Hotel Details', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/12_hotel_photos.png', 
        	title: 'Hotel Photos', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/13_car_search.png', 
        	title: 'Car Search', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/14_car_results.png', 
        	title: 'Car Results', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/15_car_voice_book.png', 
        	title: 'Car Voice Book', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/16_car_details.png', 
        	title: 'Car Details', 
        	category: 'Travel',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/17_receipt.png', 
        	title: 'Receipt', 
        	category: 'Expense',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/18_expense.png', 
        	title: 'Expense', 
        	category: 'Expense',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/19_expense_list.png', 
        	title: 'Expense List', 
        	category: 'Expense',
        	platform: 'Android'
        },
        {
        	url: 'img/marketing/android/20_reports.png', 
        	title: 'Reports', 
        	category: 'Expense',
        	platform: 'Android'
        }
    ];
});