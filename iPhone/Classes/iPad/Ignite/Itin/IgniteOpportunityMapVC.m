//
//  IgniteOpportunityMapVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 9/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteOpportunityMapVC.h"
#import "ImageUtil.h"
#import "SalesForceCOLAManager.h"
#import "TripManager.h"
#import "SegmentData.h"
#import "DateTimeFormatter.h"
#import "IgniteMeetingDetailVC.h"

@interface IgniteOpportunityMapVC (Private)

-(void)updateMap;
-(void)configureNavBar;
- (void)buttonClosePressed;

@end

@implementation IgniteOpportunityMapVC
@synthesize mapView, currentLoc, lstOpportunityId, trip, segment;
@synthesize delegate = _delegate;

static NSMutableArray* lstLocations = nil;


+ (IgniteVendorAnnotation *) makeLocation:(NSString*)id latitude:(NSString*) lat longitude:(NSString*) lon
{
    IgniteVendorAnnotation * result = [[IgniteVendorAnnotation alloc] init];
    result.name = id;
    result.longitude = lon;
    result.latitude = lat;
    result.type = SEG_TYPE_EVENT;
    return result;
}

+ (void)initialize
{
	if (self == [IgniteOpportunityMapVC class]) 
	{
        // Perform initialization here.
        lstLocations = [[NSMutableArray alloc] initWithObjects:
                        [self makeLocation:@"0" latitude:@"37.787912" longitude:@"-122.410959"],
                        [self makeLocation:@"1" latitude:@"37.792338" longitude:@"-122.41053"],
                        [self makeLocation:@"2" latitude:@"37.787013" longitude:@"-122.404993"],
                        [self makeLocation:@"3" latitude:@"37.788878" longitude:@"-122.402805"],
                        [self makeLocation:@"4" latitude:@"37.789251" longitude:@"-122.403813"],
                        [self makeLocation:@"5" latitude:@"37.790591" longitude:@"-122.399246"],
                        [self makeLocation:@"6" latitude:@"37.787217" longitude:@"-122.39847"],
                        [self makeLocation:@"7" latitude:@"37.790693" longitude:@"-122.399461"],
                        [self makeLocation:@"8" latitude:@"37.787844" longitude:@"-122.398688"],
                        [self makeLocation:@"9" latitude:@"37.790676" longitude:@"-122.404414"],
                        nil];
    }
}
                                       
                                       
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)dealloc
{
    self.delegate = nil;
    mapView.delegate = nil; 
}

- (void)viewDidLoad
{
    self.title = @"Nearby Opportunities";
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self configureNavBar];
    [self.navigationController setToolbarHidden:YES];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateMap];
}

-(void) configureNavBar
{
    // Show custom nav bar
    UIImage *imgNavBar = [ImageUtil getImageByName:@"bar_title_landscape"];
    self.navigationController.navigationBar.tintColor = [UIColor clearColor];
    [self.navigationController.navigationBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsLandscapePhone];
    
    UIBarButtonItem* btnClose = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Close" SelectorString:@"buttonClosePressed" MobileVC:self];
    
	[self.navigationItem setLeftBarButtonItem:btnClose animated:NO];    
    
}


#pragma mark - Button handlers
- (void)buttonClosePressed
{
    // Need to inform parent to close the popover vc
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark -
#pragma mark Map methods
-(void)selectAnnotation:(IgniteVendorAnnotation*) annotation
{
    [mapView selectAnnotation:annotation animated:YES];
}

-(void)updateMap
{
	// Remove all existing annotations
	[mapView removeAnnotations:[mapView annotations]];
    
	double boxWidth = 0.02;
	double boxHeight = 0.02;
	
	// The center location is the location around which the map will be centered.
	CLLocationCoordinate2D centerLocation;
	centerLocation.latitude = [currentLoc.latitude doubleValue];
	centerLocation.longitude = [currentLoc.longitude doubleValue];
	
	// The span is as big as it needs to be to show all the hotel
	MKCoordinateSpan span;
	span.latitudeDelta = boxWidth;
	span.longitudeDelta = boxHeight;
	
	// Set the map region
	MKCoordinateRegion region;
	region.center = centerLocation;
	region.span = span;
	
	[mapView setRegion:region animated:YES];
	[mapView regionThatFits:region];
    
    // update currentLoc hotelname from trip
    NSSet* segments = self.trip.relSegment;
    for (EntitySegment *s in segments)
    {
        if ([SEG_TYPE_HOTEL isEqualToString:s.type])
        {
            if ([s.segmentName length])
                currentLoc.name = s.segmentName;
            else if ([s.vendorName length])
                currentLoc.name = s.vendorName;
            else {
                currentLoc.name = s.vendor;
            }
            
//            if(s.relStartLocation.address != nil)
//                currentLoc.address = s.relStartLocation.address;
            
//            currentLoc.cityzip = [SegmentData getCityStateZip:s.relStartLocation];
        }
    }
    
    [mapView addAnnotation:currentLoc];
    int count = [self.lstOpportunityId count];
    if (count > 10)
        count = 10;
    for (int ix = 0; ix < count; ix++)
    {
        IgniteVendorAnnotation* ann = [lstLocations objectAtIndex:ix];
        ann.name = [self.lstOpportunityId objectAtIndex:ix];
        [mapView addAnnotation:ann];
    }
    
    [self performSelector:@selector(selectAnnotation:) withObject:self.currentLoc afterDelay:1.8f];

}

#pragma mark -
#pragma mark MKMapViewDelegate methods


-(MKAnnotationView *)mapView:(MKMapView *)thismapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    __autoreleasing MKPinAnnotationView *annView=[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"currentloc"];
    if (![annotation isKindOfClass:[IgniteVendorAnnotation class]])
        return annView;
    //    annView.image = [UIImage imageNamed:@"ignite_map_pin"];
    IgniteVendorAnnotation* ann = (IgniteVendorAnnotation*) annotation;
    if ([SEG_TYPE_EVENT isEqualToString:ann.type])
    {
        annView.pinColor = MKPinAnnotationColorRed;
        annView.animatesDrop=TRUE;
        annView.canShowCallout = NO;
    }
    else 
    {
        annView.pinColor = MKPinAnnotationColorPurple;
        annView.animatesDrop=NO;
        annView.canShowCallout = YES;
        annView.calloutOffset = CGPointMake(-6, 1);//CGPointMake(0, 1);
        annView.leftCalloutAccessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"icon_hotel_map_anno"]];
        
    }
    return annView;
}

- (void)mapView:(MKMapView *)mapVw didSelectAnnotationView:(MKAnnotationView *)vw
{
    if (![vw.annotation isKindOfClass:[IgniteVendorAnnotation class]])
        return;

    IgniteVendorAnnotation* ann = (IgniteVendorAnnotation*) vw.annotation;
    if (![SEG_TYPE_EVENT isEqualToString: ann.type])
        return;
    
    NSString *oppId = ann.name;
    EntitySalesOpportunity* opp = [[SalesForceCOLAManager sharedInstance] fetchOpportunityByOppId:oppId];
    
    NSString *startDateStr = self.trip.tripStartDateLocal;
    NSDate *startDate = [DateTimeFormatter getNSDate:startDateStr Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSDate *startDateAtDawn = [DateTimeFormatter getDateWithoutTimeInGMT:startDate];
    NSDate *mtgStartDate = [startDateAtDawn dateByAddingTimeInterval:60*6*145]; // 2:30pm
    NSDate *mtgEndDate = [startDateAtDawn dateByAddingTimeInterval:60*6*155]; // 3:30pm
    

    self.segment = [TripManager makeNewSegment:self.trip manContext:[[SalesForceCOLAManager sharedInstance] context]];
    segment.segmentName = @"Unscheduled Meeting";
    [SegmentData setAttribute:@"OpportunityId" withValue:opp.opportunityId toSegment:segment];
    [SegmentData setAttribute:@"OpportunityName" withValue:opp.opportunityName toSegment:segment];
    [SegmentData setAttribute:@"AccountName" withValue:opp.accountName toSegment:segment];
    [SegmentData setAttribute:@"ContactName" withValue:opp.contactName toSegment:segment];
    segment.type = SEG_TYPE_EVENT;
    segment.relStartLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:mtgStartDate];
    segment.relEndLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:mtgEndDate];
    segment.status = STATUS_SEGMENT_UNSCHEDULED;
    [TripManager saveItWithContext:segment manContext:[[SalesForceCOLAManager sharedInstance] context]];

    IgniteMeetingDetailVC *mtgVc = [[IgniteMeetingDetailVC alloc] initWithNibName:@"IgniteMeetingDetailVC" bundle:nil];
    mtgVc.contentSizeForViewInPopover = CGSizeMake(480, 600); // size of view in popover
    mtgVc.modalInPopover = YES; // Clicks outside the popover are ignored
    [mtgVc setSeedData:self withSegment:segment];
    
    self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:mtgVc];

    CGPoint annotationPoint = [mapView convertCoordinate:vw.annotation.coordinate toPointToView:mapView];
    CGRect myRect = CGRectMake(annotationPoint.x-6, annotationPoint.y-36, 14, 12);// y-1
    [self.pickerPopOver presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft|UIPopoverArrowDirectionRight animated:YES];    

}

#pragma mark - IgniteSegmentEditDelegate methods
- (void) segmentUpdated:(EntitySegment*) segment
{
    self.segment.relTrip = self.trip;
    [TripManager saveItWithContext:self.segment manContext:[[SalesForceCOLAManager sharedInstance] context]];
    
    // reorder segments and reload table.
    [self.delegate segmentUpdated:self.segment];
    
    [self.pickerPopOver dismissPopoverAnimated:NO];
    [self buttonClosePressed];
}

- (void)dismissPopoverModal
{
    [[TripManager sharedInstance] deleteObj:self.segment];
    self.segment = nil;
    [self.pickerPopOver dismissPopoverAnimated:YES];
}

//
//-(void) mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control
//{
//    IgniteVendorAnnotation* ann = (IgniteVendorAnnotation*) view.annotation;
//    
//    IgniteRecommendationVC *mvc = [[[IgniteRecommendationVC alloc] initWithNibName:@"IgniteRecommendationVC" bundle:nil] autorelease];
//    [mvc setSeedData:self loc:ann seg:self.segment];
//    mvc.modalPresentationStyle = UIModalPresentationFormSheet;
//    [self presentViewController:mvc animated:YES completion:nil];
//    
//    //    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
//    //    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
//    //    [localNavigationController setToolbarHidden:NO];
//    //    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
//    //    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
//    //    
//    //    [self presentViewController:localNavigationController animated:YES completion:nil];
//    
//}

@end
