//
//  IgniteDiningMapVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteDiningMapVC.h"
#import "ImageUtil.h"
#import "IgniteRecommendationVC.h"
#import "IgniteCuisineSelectVC.h"

@interface IgniteDiningMapVC (Private)
-(void)updateMap;
-(void)configureNavBar;
- (void)buttonClosePressed;
- (void)buttonCuisinePressed:(id) sender;

@end

@implementation IgniteDiningMapVC
@synthesize mapView, vwCuisine, tblCuisinePicker, currentLoc, lstCuisine, selectedCuisine, segment;
@synthesize delegate = _delegate;
@synthesize vcPopover;

static NSMutableDictionary* cuisineDict = nil;      // cuisine => list of restaurant names, location, etc.

static NSString *kAllCuisine = @"All";

+ (NSMutableDictionary*) getCuisineMap
{
	return cuisineDict;
}

+ (IgniteVendorAnnotation *) makeLocation:(NSString*)name latitude:(NSString*) lat longitude:(NSString*) lon  address:(NSString*) address cityzip:(NSString*) city reviews:(NSString*) rev
{
    IgniteVendorAnnotation * result = [[IgniteVendorAnnotation alloc] init];
    result.name = name;
    result.address = address;
    result.longitude = lon;
    result.latitude = lat;
    result.cityzip = city;
    result.reviewSummary = rev;
    result.type = SEG_TYPE_DINING;
    return result;
}

+ (void)initialize
{
	if (self == [IgniteDiningMapVC class]) 
	{
        // Perform initialization here.
		cuisineDict = [[NSMutableDictionary alloc] init];
        NSArray* italianRestaurants = [NSArray arrayWithObjects:
                                    [self makeLocation:@"Ducca" latitude:@"37.7872" longitude:@"-122.403148" address:@"50 3rd Street" cityzip:@"San Francisco, CA 94103" reviews:@"3 reviews"],
                                    [self makeLocation:@"Pazzia Restaurant" latitude:@"37.783706" longitude:@"-122.398148" address:@"337 3rd Street" cityzip:@"San Francisco, CA"  reviews:@"14 reviews"],
                                    [self makeLocation:@"Buca di Beppo" latitude:@"37.782468" longitude:@"-122.403749" address:@"855 Howard Street" cityzip:@"San Francisco, CA"  reviews:@"22 reviews" ],
                                    [self makeLocation:@"Restaurante Umbria" latitude:@"37.787217" longitude:@"-122.39847"	address:@"98 2nd Street" cityzip:@"San Francisco, CA"  reviews:@"8 reviews"],
                                       [self makeLocation:@"54 Mint Restaurant" latitude:@"37.783096" longitude:@"-122.407804" address:@"16 Mint Plaza" cityzip:@"San Francisco, CA"  reviews:@"6 reviews"], nil];
        [cuisineDict setObject:italianRestaurants forKey:@"Italian"];
        NSArray* japaneseRestaurants = [NSArray arrayWithObjects:
                                       [self makeLocation:@"Shiki Japanese Restaurant" latitude:@"37.784893" longitude:@"-122.399522" address:@"251 3rd Street" cityzip:@"San Francisco, CA 94103" reviews:@"3 reviews"],
                                       [self makeLocation:@"Ame Restaurant" latitude:@"37.786793" longitude:@"-122.401582" address:@"689 Mission Street" cityzip:@"San Francisco, CA"  reviews:@"12 reviews"],
                                       [self makeLocation:@"Kyo-Ya" latitude:@"37.788997" longitude:@"-122.402032" address:@"2 New Montgomery Street" cityzip:@"San Francisco, CA"  reviews:@"28 reviews" ],
                                       [self makeLocation:@"Sanraku" latitude:@"37.785012" longitude:@"-122.403212"	address:@"101 4th Street" cityzip:@"San Francisco, CA"  reviews:@"8 reviews"],
                                       [self makeLocation:@"Anzu Restaurant & Bar" latitude:@"37.786199" longitude:@"-122.409564" address:@"222 Mason Street" cityzip:@"San Francisco, CA"  reviews:@"18 reviews"], nil];
        [cuisineDict setObject:japaneseRestaurants forKey:@"Japanese"];
        NSArray* americanRestaurants = [NSArray arrayWithObjects:
                                        [self makeLocation:@"Oola Restaurant & Bar" latitude:@"37.781552" longitude:@"-122.402569" address:@"860 Folsom Street" cityzip:@"San Francisco, CA 94103" reviews:@"3 reviews"],
                                        [self makeLocation:@"Annabelle's Bar & Bistro" latitude:@"37.7853" longitude:@"-122.404993" address:@"68 4th Street" cityzip:@"San Francisco, CA"  reviews:@"12 reviews"],
                                        [self makeLocation:@"American Grilled Cheese Kitchen" latitude:@"37.782706" longitude:@"-122.392784" address:@"1 South Park Street" cityzip:@"San Francisco, CA"  reviews:@"28 reviews" ],
                                        [self makeLocation:@"Fifth Floor" latitude:@"37.785724" longitude:@"-122.405315"	address:@"12 4th Street" cityzip:@"San Francisco, CA"  reviews:@"8 reviews"],
                                        [self makeLocation:@"Luce Restaurant" latitude:@"37.782078" longitude:@"-122.404908" address:@"888 Howard Street" cityzip:@"San Francisco, CA"  reviews:@"18 reviews"], nil];
        [cuisineDict setObject:americanRestaurants forKey:@"American"];
        NSArray* frenchRestaurants = [NSArray arrayWithObjects:
                                       [self makeLocation:@"Le Charm French Bistro" latitude:@"37.780535" longitude:@"-122.402976" address:@"315 5th Street" cityzip:@"San Francisco, CA 94103" reviews:@"36 reviews"],
                                       [self makeLocation:@"Restaurant Lulu" latitude:@"37.782146" longitude:@"-122.401732" address:@"816 Folsom Street" cityzip:@"San Francisco, CA"  reviews:@"14 reviews"],
                                       [self makeLocation:@"Chez Papa Resto" latitude:@"37.783435" longitude:@"-122.407633" address:@"4 Mint Plaza" cityzip:@"San Francisco, CA"  reviews:@"22 reviews" ],
                                       [self makeLocation:@"Cafe Claude" latitude:@"37.790676" longitude:@"-122.404414"	address:@"7 Claude Lane" cityzip:@"San Francisco, CA"  reviews:@"8 reviews"],
                                       [self makeLocation:@"South Park Cafe" latitude:@"37.781959" longitude:@"-122.394372" address:@"108 South Park Street" cityzip:@"San Francisco, CA"  reviews:@"6 reviews"], nil];
        [cuisineDict setObject:frenchRestaurants forKey:@"French"];
        NSArray* chineseRestaurants = [NSArray arrayWithObjects:
                                        [self makeLocation:@"Fang Restaurant" latitude:@"37.786691" longitude:@"-122.399675" address:@"660 Howard Street" cityzip:@"San Francisco, CA 94103" reviews:@"3 reviews"],
                                        [self makeLocation:@"Yank Sing" latitude:@"37.790693" longitude:@"-122.399461" address:@"49 Stevenson Street" cityzip:@"San Francisco, CA"  reviews:@"12 reviews"],
                                        [self makeLocation:@"R&G Lounge" latitude:@"37.790591" longitude:@"-122.399246" address:@"631 Kearny Street" cityzip:@"San Francisco, CA"  reviews:@"28 reviews" ],
                                        [self makeLocation:@"Heaven's Dog" latitude:@"37.779229" longitude:@"-122.412078"	address:@"1148 Mission Street" cityzip:@"San Francisco, CA"  reviews:@"8 reviews"],
                                        [self makeLocation:@"Henry's Hunan Restaurant" latitude:@"37.787844" longitude:@"-122.398688" address:@"110 Natoma Street" cityzip:@"San Francisco, CA"  reviews:@"18 reviews"], nil];
        [cuisineDict setObject:chineseRestaurants forKey:@"Chinese"];
 	}
}

    /*    Italian	Ducca	37.7872,-122.403148	50 3rd Street, San Francisco, CA
     Italian	Pazzia Restaurant	37.783706,-122.398148	337 3rd Street, San Francisco, CA
     Italian	Buca di Beppo	37.782468,-122.403749	855 Howard Street, San Francisco, CA
     Italian	Restaurante Umbria	37.787217,-122.39847	98 2nd Street, San Francisco, CA
     Italian	54 Mint Restaurant	37.783096,-122.407804	16 Mint Plaza, San Francisco, CA
     
     Japanese	Shiki Japanese Restaurant	37.784893,-122.399522	251 3rd Street, San Francisco, CA
     Japanese	Ame Restaurant	37.786793,-122.401582	689 Mission Street, San Francisco, CA
     Japanese	Kyo-Ya	37.788997,-122.402032	2 New Montgomery Street, San Francisco, CA
     Japanese	Sanraku	37.785012,-122.403212	101 4th Street, San Francisco, CA
     Japanese	Anzu Restaurant & Bar	37.786199,-122.409564	222 Mason Street, San Francisco, CA
    
     American	Oola Restaurant & Bar	37.781552,-122.402569	860 Folsom Street, San Francisco, CA
     American	Annabelle's Bar & Bistro	37.7853,-122.404993	68 4th Street, San Francisco, CA
     American	American Grilled Cheese Kitchen	37.782706,-122.392784	1 South Park Street, San Francisco, CA
     American	Fifth Floor	37.785724,-122.405315	12 4th Street, San Francisco, CA
     American	Luce Restaurant	37.782078,-122.404908	888 Howard Street, San Francisco, CA
     
     French	Le Charm French Bistro	37.780535,-122.402976	315 5th Street, San Francisco, CA
     French	Restaurant Lulu	37.782146,-122.401732	816 Folsom Street, San Francisco, CA
     French	Chez Papa Resto	37.783435,-122.407633	4 Mint Plaza, San Francisco, CA
     French	Cafe Claude	37.790676,-122.404414	7 Claude Lane, San Francisco, CA
     French	South Park Cafe	37.781959,-122.394372	108 South Park Street, San Francisco, CA
     
     Chinese	Fang Restaurant	37.786691,-122.399675	660 Howard Street, San Francisco, CA
     Chinese	Yank Sing	37.790693,-122.399461	49 Stevenson Street, San Francisco, CA
     Chinese	R&G Lounge	37.790591,-122.399246	631 Kearny Street, San Francisco, CA
     Chinese	Heaven's Dog	37.779229,-122.412078	1148 Mission Street, San Francisco, CA
     Chinese	Henry's Hunan Restaurant	37.787844,-122.398688	110 Natoma Street, San Francisco, CA
     */


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.lstCuisine = [NSArray arrayWithObjects:kAllCuisine, @"American", @"Chinese", @"French", @"Italian", @"Japanese", nil];
    }
    return self;
}

- (void)viewDidLoad
{
    self.title = @"Dining Recommendations";
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self configureNavBar];
    [self.navigationController setToolbarHidden:YES];
    self.selectedCuisine = nil;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)setSeedData:(id<IgniteSegmentEditDelegate>) del loc:(IgniteVendorAnnotation*)annotation seg:(EntitySegment*) seg
{
    self.delegate = del;
    self.currentLoc = annotation;
    self.segment = seg;
}

- (void)dealloc
{
    // If deallocated quickly, dismiss the popover
    if (self.vcPopover != nil)
        [self.vcPopover dismissPopoverAnimated:NO];
    mapView.delegate = nil; 
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
    if (self.selectedCuisine == nil)
    {
        // First time open this view
        self.selectedCuisine = kAllCuisine;
        [self performSelector:@selector(buttonCuisinePressed:) withObject:nil afterDelay:1.5f];
    }
    
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

    UIBarButtonItem* btnCuisine = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Cuisine" SelectorString:@"buttonCuisinePressed:" MobileVC:self];

	[self.navigationItem setRightBarButtonItem:btnCuisine animated:NO];    
}


#pragma mark - Button handlers
- (void)buttonClosePressed
{
    // Need to inform parent to close the popover vc
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)buttonCuisinePressed:(id) sender
{
    IgniteCuisineSelectVC *cvc = [[IgniteCuisineSelectVC alloc] initWithNibName:@"IgniteCuisineSelectVC" bundle:nil];
    cvc.contentSizeForViewInPopover = CGSizeMake(200, 280); // size of view in popover
    [cvc setSeedData:self];
    if (self.vcPopover != nil)
    {
        [self.vcPopover dismissPopoverAnimated:NO];
        self.vcPopover = nil;
    }
    self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:cvc];
    CGRect myRect = CGRectMake(self.navigationController.navigationBar.frame.size.width-80, 0, 60, 1);
    [vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];    
    
    int selectedIndex = 0;
    for (int ix = 0; ix < [self.lstCuisine count]; ix++)
    {
        if ([[lstCuisine objectAtIndex:ix] isEqualToString:self.selectedCuisine])
            selectedIndex = ix;
    }
    NSIndexPath *ixPath = [NSIndexPath indexPathForRow:selectedIndex inSection:0];
    
    [cvc.tableList selectRowAtIndexPath:ixPath animated:NO scrollPosition:UITableViewScrollPositionTop];

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
	
    NSArray * restaurants = [[IgniteDiningMapVC getCuisineMap] objectForKey:selectedCuisine];
    
    if ([kAllCuisine isEqualToString:selectedCuisine])
    {
        NSMutableArray *allRest = [[NSMutableArray alloc] init];
        for (NSString * cuisine in self.lstCuisine)
        {
            [allRest addObjectsFromArray:[[IgniteDiningMapVC getCuisineMap] objectForKey:cuisine]];
        }
        restaurants = allRest;
    }
    
    for (IgniteVendorAnnotation* ann in restaurants)
    {
        [mapView addAnnotation:ann];
    }
    
    if ([self.selectedCuisine isEqualToString:@"Italian"])
        [self performSelector:@selector(selectAnnotation:) withObject:[restaurants objectAtIndex:0] afterDelay:1.8f];
//	// Add the annotations (push pins)
//	[mapView addAnnotation:self.currentLoc];
//    [mapView selectAnnotation:self.currentLoc animated:FALSE];
    
}

#pragma mark -
#pragma mark MKMapViewDelegate methods


-(MKAnnotationView *)mapView:(MKMapView *)thismapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    __autoreleasing MKPinAnnotationView *annView=[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"currentloc"];
//    annView.image = [UIImage imageNamed:@"ignite_map_pin"];
    annView.pinColor = MKPinAnnotationColorRed;
    annView.animatesDrop=TRUE;
    annView.canShowCallout = YES;
    annView.calloutOffset = CGPointMake(-6, 1);//CGPointMake(0, 1);
    annView.leftCalloutAccessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"review_stars_4_5"]];
    annView.rightCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
    return annView;
}

-(void) mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control
{
    IgniteVendorAnnotation* ann = (IgniteVendorAnnotation*) view.annotation;
    
    IgniteRecommendationVC *mvc = [[IgniteRecommendationVC alloc] initWithNibName:@"IgniteRecommendationVC" bundle:nil];
    [mvc setSeedData:self loc:ann seg:self.segment];
    mvc.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:mvc animated:YES completion:nil];
    
//    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
//    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
//    [localNavigationController setToolbarHidden:NO];
//    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
//    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
//    
//    [self presentViewController:localNavigationController animated:YES completion:nil];

}

//- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView2
//{
//	[mapView2 selectAnnotation:annotationToSelect animated:FALSE];
//}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [lstCuisine count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSString *CellIdentifier = @"CuisineCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    NSString* cuisine = [lstCuisine objectAtIndex:indexPath.row];
    cell.textLabel.text = cuisine;
    cell.selectionStyle = UITableViewCellSelectionStyleGray;
    cell.textLabel.textColor = [UIColor blackColor];
    cell.textLabel.highlightedTextColor = [UIColor blackColor];
    cell.textLabel.textAlignment = NSTextAlignmentCenter;
    if ([cuisine isEqualToString:self.selectedCuisine])
        [cell setAccessoryType:UITableViewCellAccessoryCheckmark];
    else {
        [cell setAccessoryType:UITableViewCellAccessoryNone];
    }
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.selectedCuisine = [lstCuisine objectAtIndex:indexPath.row];
    [self updateMap];
    [tableView reloadData];
}

- (void) segmentUpdated:(EntitySegment*) seg
{
    if (seg != nil)
    {
        [self.delegate segmentUpdated:seg];
        [self buttonClosePressed];
    }
}

//#pragma mark -
//#pragma mark UIPickerViewDelegate methods
//
//-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
//{
//	return [lstCuisine objectAtIndex:row];
//}
//
//-(void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
//{
//    [self updateMap];
//}
//
//#pragma mark -
//#pragma mark UIPickerViewDataSource methods
//
//-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
//{
//	return 1;
//}
//
//-(NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
//{
//	return [lstCuisine count];
//}
//

@end
