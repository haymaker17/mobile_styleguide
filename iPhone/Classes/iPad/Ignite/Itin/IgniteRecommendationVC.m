//
//  IgniteRecommendationVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteRecommendationVC.h"
#import "ImageUtil.h"
#import "SegmentData.h"
#import "IgniteReviewCell.h"
#import "SocialReview.h"
#import "DateTimeFormatter.h"

@interface IgniteRecommendationVC ()
- (void)configureNavBar;
@end

@implementation IgniteRecommendationVC
@synthesize tableList, navBar, tBar, lblAddress, lblCity, lblVendorName, vwHeader, ivVendor, btnPhone, ivStarRating;
@synthesize vwReviewTitle, lblReviewTitle, btnWriteReview;
@synthesize segment, reviews, vendor;
@synthesize delegate = _delegate;

static NSMutableDictionary* reviewDict = nil;       // restaurant name => list of reviews
+ (SocialReview *) makeReview:(NSString*)uname location:(NSString*)loc profileImage:(NSString*) imgName review:(NSString*) rev date:(NSString*)revDate rating:(NSInteger) rat
{
    SocialReview * result = [[SocialReview alloc] init];
    result.userName = uname;
    result.location = loc;
    result.profileImageName = imgName;
    result.review = rev;
    result.date = revDate;
    result.rating = rat;
    return result;
}

+ (void)initialize
{
	if (self == [IgniteRecommendationVC class]) 
	{
        // Perform initialization here.
		reviewDict = [[NSMutableDictionary alloc] init];
        NSArray* duccaReviews = [NSArray arrayWithObjects:
                                 [self makeReview:@"Dave Best" location:@"Concur, Redmond" profileImage:@"man3" review:@"Excellent hotel restaurant with good bar scene. Menu specials are well prepared and consistently good. You can find a quiet corner here for drinks and dinner." date:@"8/31/2012" rating:5],
                                 [self makeReview:@"Bernadette Rubio" location:@"Concur, San Francisco" profileImage:@"woman3" review:@"If you're staying in the hotel, this is a really convenient place to grab a bit to eat and meet with your team. The food is reasonably priced and they have most of the standards. I'd recommend if you have to get to and from the convention center quickly as well." date:@"8/12/2012" rating:4], 
                                 [self makeReview:@"Jay Stevens" location:@"Concur, Chicago" profileImage:@"man1" review:@"Great atmosphere and easy to have conversations without having to be too quiet. Food is great and the drinks are even better. I usually use this place for business lunches when I'm attending events at the convention center." date:@"5/23/2012" rating:5],
                                 nil];
        [reviewDict setObject:duccaReviews forKey:@"Ducca"];
    }
}

- (void)setSeedData:(id<IgniteSegmentEditDelegate>) del loc:(IgniteVendorAnnotation*)annotation seg:(EntitySegment*) seg;
{
    self.segment = seg;
    self.delegate = del;
    self.vendor = annotation;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self configureNavBar];
    
    if (self.vendor != nil)
    {
        self.lblVendorName.text = self.vendor.name;
        self.lblAddress.text = self.vendor.address;
        self.lblCity.text = self.vendor.cityzip;
        self.ivStarRating.image = [UIImage imageNamed:@"review_stars_4_5"];
        UIImage* img = [UIImage imageNamed:@"dining_details_Ducca"];
        self.ivVendor.image = img;
        self.lblReviewTitle.text = [NSString stringWithFormat:@"%@ Reviews", self.vendor.name];
    }
}

-(void) configureNavBar
{
    // Show custom nav bar
    UIImage *imgNavBar = [ImageUtil getImageByName:@"bar_title_landscape"];
    navBar.tintColor = [UIColor clearColor];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsDefault];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsLandscapePhone];
    
    UINavigationItem *navItem = [UINavigationItem alloc];
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 360, 44)];
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont boldSystemFontOfSize:16.0];
    label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
    label.textAlignment = NSTextAlignmentCenter;
    label.textColor =[UIColor whiteColor];
    label.text = @"Recommendations Details";		
    navItem.titleView = label;
    
    UIBarButtonItem* btnCancel = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Cancel" SelectorString:@"buttonCancelPressed" MobileVC:self];
    
    
    UIBarButtonItem* btnReserve = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:150 H:31 Text:(NSString *)@"Make Reservation" SelectorString:@"buttonReservePressed" MobileVC:self];
    
	[navItem setLeftBarButtonItem:btnCancel animated:NO];
	[navItem setRightBarButtonItem:btnReserve animated:NO];
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark - Button handlers
- (void)buttonCancelPressed
{
    [self.presentingViewController dismissModalViewControllerAnimated:YES];
    // Need to inform parent to close the popover vc
    [self.delegate segmentUpdated:nil];
}

- (void)buttonReservePressed
{
    segment.status = @"";
    [SegmentData setAttribute:@"VendorName" withValue:self.vendor.name toSegment:self.segment];
    [SegmentData setAttribute:@"Address" withValue:[NSString stringWithFormat:@"%@, %@", self.vendor.address, self.vendor.cityzip] toSegment:self.segment];
    self.segment.segmentName = @"Dining";

    // Fix dining time
    NSDate *startDate = [DateTimeFormatter getNSDate:self.segment.relStartLocation.dateLocal Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSDate *startDateAtDawn = [DateTimeFormatter getDateWithoutTimeInGMT:startDate];
    NSDate *diningStartDate = [startDateAtDawn dateByAddingTimeInterval:60*6*195]; // 19:30:00pm
    NSDate *diningEndDate = [startDateAtDawn dateByAddingTimeInterval:60*6*205]; // 20:30:00pm
    segment.relStartLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:diningStartDate];
    segment.relEndLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:diningEndDate];

    
    [self.presentingViewController dismissModalViewControllerAnimated:NO];
    // Need to inform parent to close the popover vc
    [self.delegate segmentUpdated:segment];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 3; //[lstCuisine count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSString *CellIdentifier = @"IgniteReviewCell";
    IgniteReviewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteReviewCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteReviewCell class]])
                cell = (IgniteReviewCell *)oneObject;
    }
    
    SocialReview* rev = (SocialReview*)[[reviewDict objectForKey:@"Ducca"] objectAtIndex:indexPath.row];
    cell.ivProfile.image = [UIImage imageNamed:rev.profileImageName];
    if (rev.rating == 4)
        cell.ivRating.image = [UIImage imageNamed:@"review_stars_4"];        
    else
        cell.ivRating.image = [UIImage imageNamed:@"review_stars_5"];
    
    cell.lblDate.text = rev.date;
    cell.lblLocation.text = rev.location;
    cell.lblUserName.text = rev.userName;
    cell.lblReview.text = rev.review;
    
    return cell;
}

-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 132;
}

@end
