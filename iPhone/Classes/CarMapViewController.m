//
//  CarMapViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarMapViewController.h"
#import "ExSystem.h" 

#import "Car.h"
#import "HotelAnnotation.h"


@implementation CarMapViewController

@synthesize mapView;
@synthesize screenTitleBarButtonItem;
@synthesize car, toolBar;


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return CAR_MAP;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(void)respondToFoundData:(Msg *)msg
{
}


#pragma mark -
#pragma mark Initialization

/*
- (id)initWithStyle:(UITableViewStyle)style {
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
    if ((self = [super initWithStyle:style])) {
    }
    return self;
}
*/


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
	
	[self updateMap];
	
	screenTitleBarButtonItem.title = car.chainName;
	
	if ([UIDevice isPad]) {
//		localNavigationController.toolbar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
//		localNavigationController.navigationBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	}

    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    self.toolBar.tintColor = [UIColor darkBlueConcur_iOS6];
    self.toolBar.alpha = 0.9f;
    
}

/*
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}
*/
/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}
*/

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


#pragma mark -
#pragma mark Map methods
-(void)updateMap
{
	// Remove all existing annotations
	[mapView removeAnnotations:[mapView annotations]];
	
	// Return if there's nothing to show
	if (car == nil)
		return;
	
	double pickupLatitude = car.pickupLocationLatitude;
	double pickupLongitude = car.pickupLocationLongitude;
	
	// Return if we cannot obtain the latitude and longitude of the pickup location
	//if (pickupLatitude == 0 && pickupLongitude == 0)
	//	return;
	
	// The center location is the location around which the map will be centered.
	CLLocationCoordinate2D centerLocation;
	centerLocation.latitude = pickupLatitude;
	centerLocation.longitude = pickupLongitude;
	
	// The span is a default size
	MKCoordinateSpan span;
	span.latitudeDelta = 0.005;
	span.longitudeDelta = 0.005;
	
	// Set the map region
	MKCoordinateRegion region;
	region.center = centerLocation;
	region.span = span;
	
	[mapView setRegion:region animated:YES];
	[mapView regionThatFits:region];
	
	// Add the annotation (push pins)
	CLLocationCoordinate2D pickupCoordinate;
	pickupCoordinate.latitude = pickupLatitude;
	pickupCoordinate.longitude = pickupLongitude;
	
	// Using HotelAnnotation class for now
	HotelAnnotation *pickupAnnotation = [HotelAnnotation alloc];
	pickupAnnotation.coordinate = pickupCoordinate;
	pickupAnnotation.title = car.pickupLocationName;
	pickupAnnotation.subtitle = car.pickupLocationAddress;
	
	[mapView addAnnotation:pickupAnnotation];
	
}


#pragma mark -
#pragma mark Buttons

-(IBAction) btnDone:(id)sender
{
    if([UIDevice isPad])
        [self dismissViewControllerAnimated:YES completion:nil];
    else
        [self dismissViewControllerAnimated:YES completion:nil];	
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
    self.toolBar = nil;
}


- (void)dealloc
{
	mapView.delegate = nil;
}


@end

