//
//  MapViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/20/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "MapViewController.h"
#import "MyAnnotation.h"
#import "ExSystem.h" 

#import "FindAddress.h"
#import "Location.h"
#import "MobileAlertView.h"

@interface MapViewController()
@property (nonatomic) NSString *locationErrorMessage;
@end

@implementation MapViewController

-(IBAction)closeMe:(id)sender
{
    //MOB-10941
    if ([self.navigationController.viewControllers count]>1) // more than one view so just pop back to previous view
    {
        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        [self dismissViewControllerAnimated:YES completion:nil]; // Only one viewcontroller on stack so close this
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(NSString *)getDisplayMethod:(NSString *)fromView
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(NSString *)getViewIDKey
{
	return MAP;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


+ (Location *)requestLocationByAddress:(NSString *)address
{
    NSString *escaped_address =  [address stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
	
    // Contact Google and make a geocoding request
	// See Google API docs at: http://googlegeodevelopers.blogspot.com/2010/03/introducing-new-google-geocoding-web.html
	// See http://code.google.com/apis/maps/documentation/geocoding/ for documentation about "results influenced by the region"
	NSString *requestString = [NSString stringWithFormat:@"https://maps.google.com/maps/api/geocode/xml?address=%@&sensor=false", escaped_address];
	
	__autoreleasing Location *loc = [[Location alloc] init];
	[loc parseXMLFileAtURL:requestString];
	return loc;
}

-(void) getLocationByAddress:(NSString *)address
{
	Location *loc = [MapViewController requestLocationByAddress:address];

	self.strLongi = loc.location.longitude;
	self.strLati = loc.location.latitude;
}

-(void) viewWillAppear:(BOOL)animated
{
    // No view on view stack behind this view controller
    // so use dismissViewController(close) instead of popViewController(back)
    if ([self.navigationController.viewControllers count] < 2)
    {
        self.navigationItem.leftBarButtonItem = nil;
        UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe:)];
        self.navigationItem.leftBarButtonItem = btn;
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    // Cover the back button (cannot do this in viewWillAppear -- too soon)
    // UIControl *backCover is the best way at the time to change the default behavior for BACK button
    // Add back button when there are views on the view stack before the current viewcontroller
    if ( backCover == nil && [self.navigationController.viewControllers count] > 1)
    {
        backCover = [[UIControl alloc] initWithFrame:CGRectMake( 0, 0, 80, 44)];
        // Uncomment these lines to see the coverage of back button
//#if TARGET_IPHONE_SIMULATOR
//        // show the cover for testing
//        backCover.backgroundColor = [UIColor colorWithRed:1.0 green:0.0 blue:0.0 alpha:0.15];
//#endif
        [backCover addTarget:self action:@selector(closeMe:) forControlEvents:UIControlEventTouchDown];
        UINavigationBar *navBar = self.navigationController.navigationBar;
        [navBar addSubview:backCover];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [backCover removeFromSuperview];
    backCover = nil;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	self.title = self.anoTitle;
	MKCoordinateRegion region = { {0.0, 0.0 }, { 0.0, 0.0 } };
    if (self.lati == nil || self.longi == nil)
    {
        // Only call the google geocode api if we do not have coords
        // TODO: replace google geocode api with CLGeocoder call, will require a little refactoring	because CLG is Async
        [self getLocationByAddress:self.mapAddress];
        region.center.latitude = [self.strLati floatValue];
        region.center.longitude = [self.strLongi floatValue];
    }
    else
    {
        region.center.latitude = [self.lati floatValue];
        region.center.longitude = [self.longi floatValue];
    }
	region.span.longitudeDelta = 0.01f;
	region.span.latitudeDelta = 0.01f;
	[self.mapView setRegion:region animated:YES];
	
	self.annotationToSelect = [[MyAnnotation alloc] init];
	self.annotationToSelect.title = self.anoTitle;// @"AVIS";
	self.annotationToSelect.subtitle = [Localizer getLocalizedText:@"Tap to get directions"];
	self.annotationToSelect.coordinate = region.center;
	[self.mapView addAnnotation:self.annotationToSelect];

	[self.mapView deselectAnnotation:self.annotationToSelect animated:YES];
    
    [self performSelector:@selector(openAnnotation:) withObject:self.annotationToSelect afterDelay:1.0];
	
    [super viewDidLoad];
}


#pragma mark - Address Stuff
- (void)openAnnotation:(id)annotation;
{
    [self.mapView selectAnnotation:annotation animated:YES];
}

-(IBAction)buttonRoutePressed:(id)sender
{
    if(self.strLongi == nil || self.strLati == nil || self.mapView.userLocation.coordinate.latitude == 0 || [self.strLongi doubleValue] == 0)
        return;
    
    NSString *latlong = [NSString stringWithFormat:@"%@,%@", self.strLati , self.strLongi];//  @"-56.568545,1.256281";
    NSString *yourLatLon = [NSString stringWithFormat:@"%.8f,%.8f", self.mapView.userLocation.coordinate.latitude, self.mapView.userLocation.coordinate.longitude];
    
    NSString *url = [NSString stringWithFormat: @"http://maps.google.com/maps?saddr=%@&daddr=%@&z=17",
                     [yourLatLon stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding],
                     [latlong stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}


- (void)dealloc 
{
	
	

	self.mapView.delegate = nil;
	

	;
	
}

#pragma mark - Map View Delegates
- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>)annotation 
{
    MKPinAnnotationView *annView=[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"Location"];
    annView.canShowCallout = YES;
    annView.calloutOffset = CGPointMake(-7, 5);
    annView.rightCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
    return annView;
}

-(void) mapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation
{
    // Reset the last detected error message if we have received a valid location
    self.locationErrorMessage = nil;
}
-(void) mapView:(MKMapView *)mapView didFailToLocateUserWithError:(NSError *)error
{
    // We have implemented this method to handle errors with Location Services and the device
    self.locationErrorMessage = nil;
    switch ([error code]) {
        case kCLErrorLocationUnknown:
        {
            // This error will occur if the location manager is unable to obtain a location at this moment
            self.locationErrorMessage = [Localizer getLocalizedText:@"MAPVIEW_LOCATION_UNKNOWN"];
            break;
        }
        case kCLErrorDenied:
        {
            // This error occurs if the App does not have permission
            self.locationErrorMessage = [Localizer getLocalizedText:@"MAPVIEW_ACCESS_DENIED"];
            break;
        }
        case kCLErrorNetwork:
        {
            // The location manager is experiencing problems accessing the cellular/wifi networks
            self.locationErrorMessage = [Localizer getLocalizedText:@"MAPVIEW_NETWORK_ERROR"];
            break;
        }
        default:
        {
            // Default error message for unknown/unexpected cause. A catch-all
            self.locationErrorMessage = [Localizer getLocalizedText:@"MAPVIEW_UNKNOWN_ERROR"];
        }
    }
    
}
-(void) mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control
{
    // Test that location services are enabled for this app
    if (self.locationErrorMessage != nil)
    {
        // Display the error message we have logged to the user
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Warning"] message:self.locationErrorMessage delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil, nil];
        [alert show];
        return;
    }
    // Test that we have a valid location
    if(self.strLongi == nil || self.strLati == nil || self.mapView.userLocation.coordinate.latitude == 0 || [self.strLongi doubleValue] == 0)
    {
        // If there is still a problem with the location, alert the user
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Warning"] message:[Localizer getLocalizedText:@"MAPVIEW_LOCATION_UNKNOWN"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil, nil];
        [alert show];
        return;
    }
    
    NSString *latlong = [NSString stringWithFormat:@"%@,%@", self.strLati , self.strLongi];//  @"-56.568545,1.256281";
    NSString *yourLatLon = [NSString stringWithFormat:@"%.8f,%.8f", self.mapView.userLocation.coordinate.latitude, self.mapView.userLocation.coordinate.longitude];
    // MOB-10653
    NSString *url = [NSString stringWithFormat: @"http://maps.apple.com/maps?saddr=%@&daddr=%@&z=17",
                     [yourLatLon stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding],
                     [latlong stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView2
{
	[mapView2 selectAnnotation:self.annotationToSelect animated:FALSE];
}

@end
