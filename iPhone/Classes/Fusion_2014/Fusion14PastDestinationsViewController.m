//
//  Fusion14PastDestinationsViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 3/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "EvaVoiceSearchViewController.h"
#import "Fusion14PastDestinationsViewController.h"
#import "Fusion14PastDestinationsTableHeaderCell.h"
#import "Fusion14HotelListCell.h"
#import "EvaVoiceSearchiOS6TableViewController.h"
/*
 * Custom class for pastDestinations
 */

@interface pastDestination : NSObject

@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *imageName;

@end

@implementation pastDestination

@end

@interface Fusion14PastDestinationsViewController ()

@property (weak, nonatomic) IBOutlet UITextView *hotelSearchTextView;
@property (weak, nonatomic) IBOutlet UIImageView *voiceSearchImageView;
@property (strong, nonatomic) NSMutableArray *pastDestinations;

@end

@implementation Fusion14PastDestinationsViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.pastDestinations = [[NSMutableArray alloc] init];
    [self loadPastDestinations];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"Fusion14_PastDestination_RightBarButton" withSelector:nil];
}

// Nav bar buttons
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
    
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    // Title should be set by the instantiating method
    [self setTableViewHeader];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)setTableViewHeader
{
    self.tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 90.0f)];
    
    Fusion14PastDestinationsTableHeaderCell *headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"Fusion14PastDestinationsTableHeaderCell"];
    [headerCell.btnVoice addTarget:self action:@selector(btnVoiceTapped) forControlEvents:UIControlEventTouchUpInside];
    [headerCell.btnTextSearch addTarget:self action:@selector(btnTextSearchTapped) forControlEvents:UIControlEventTouchUpInside];
    [self.tableView.tableHeaderView addSubview:headerCell];
}

/**
 * Loads past destinations for the user
 */

-(void)loadPastDestinations
{
    // This is a dumb implementaiton for demo.
    // Ideally this is loaded from MWS server based on the type of search that the user performed in the past.
    pastDestination *destination = [[pastDestination alloc]init];
    
    destination.city = @"New Orleans";
    destination.state = @"Louisiana";
    destination.imageName = @"Fusion14_NewOrleans";
    [self.pastDestinations addObject:destination];
    
    destination = [[pastDestination alloc]init];
    destination.city = @"New York City";
    destination.state = @"New York";
    destination.imageName = @"Fusion14_NewYork";
    [self.pastDestinations addObject:destination];
    
    destination = [[pastDestination alloc]init];
    destination.city = @"Chicago";
    destination.state = @"Illinois";
    destination.imageName = @"Fusion14_Chicago";
    [self.pastDestinations addObject:destination];

    destination = [[pastDestination alloc]init];
    destination.city = @"Los Angeles";
    destination.state = @"California";
    destination.imageName = @"Fusion14_LosAngeles";
    [self.pastDestinations addObject:destination];


}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [self.pastDestinations count] + 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 90;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    Fusion14HotelListCell *hotelListCell = [tableView dequeueReusableCellWithIdentifier:@"Fusion14HotelListCell" forIndexPath:indexPath];
    
    // stupid hack to show a "More destinations" as last row
    if ([self.pastDestinations count] == indexPath.row) {
        hotelListCell.hotelImage.image = nil;
        hotelListCell.hotelName.text = @"More destinations" ;
        hotelListCell.hotelCityAndState.text = nil;
        [hotelListCell.hotelName setTextColor:[UIColor concurBlueColor]];
        return hotelListCell;
    }

    pastDestination *destination = [self.pastDestinations objectAtIndex:indexPath.row];
    // Configure the cell...
    hotelListCell.hotelImage.image = [UIImage imageNamed:destination.imageName];
    hotelListCell.hotelName.text = destination.city;
    hotelListCell.hotelCityAndState.text = destination.state;
    
  
    return hotelListCell;
}


-(UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    return [self getHeaderView];
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    CGFloat headerHeight = [self getHeaderView].frame.size.height;
    return headerHeight;
}

#pragma mark - tableviewDelegate methods

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Do the search for given distination.
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

-(UIView*)getHeaderView
{
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.tableView.bounds.size.width, 40)];
    [headerView setBackgroundColor:[UIColor hotelsListSectionHeaderBarColor]];
    UILabel *headerTitle = [[UILabel alloc] initWithFrame:CGRectMake(15, 15, 200, 20)];
    
    [headerTitle setText:@"PAST DESTINATIONS"];
    [headerTitle setTextColor:[UIColor concurBlueColor]];
    [headerTitle setFont:[UIFont fontWithName:@"Helvetica Neue" size:14.0]];
    [headerTitle setTextAlignment:NSTextAlignmentLeft];
    
    [headerView addSubview:headerTitle];
    return headerView;
}

#pragma mark - push this view

+ (void) showPastFlightDestinations:(UINavigationController*)navi
{
    Fusion14PastDestinationsViewController *pastDestinationsViewController =
        [[UIStoryboard storyboardWithName:[@"Fusion14PastDestinations" storyboardName] bundle:nil] instantiateInitialViewController];
    
    pastDestinationsViewController.title = @"Flights";
    pastDestinationsViewController.category = EVA_FLIGHTS;
    
    // TODO : customize other stuff here.
    //
    [navi pushViewController:pastDestinationsViewController animated:YES];
}

+(void) showPastHotelDestinations:(UINavigationController *)navi
{
    Fusion14PastDestinationsViewController *pastDestinationsViewController =
        [[UIStoryboard storyboardWithName:[@"Fusion14PastDestinations" storyboardName] bundle:nil] instantiateInitialViewController];
    
    pastDestinationsViewController.title = @"Hotels";
    pastDestinationsViewController.category = EVA_HOTELS;
    
    // TODO : customize other stuff here.
    //
    [navi pushViewController:pastDestinationsViewController animated:YES];
}

-(void)btnTextSearchTapped
{
    DLog(@"Text based Search button tapped");
    UINavigationController *nav = [[UIStoryboard storyboardWithName:@"EvaVoiceSearch_iPhone" bundle:nil]
                                   instantiateInitialViewController];
    
    EvaVoiceSearchViewController *c = [nav viewControllers][0];
    c.category = self.category;
    c.isTextSearchQuery = YES;
    [self presentViewController:nav animated:YES completion:nil];

}

-(void)btnVoiceTapped
{
    // show voice view
    DLog(@"Voice button tapped");
    
    //MOB-15527 - Starting iOS7, user needs to set mic permission for each app.
    // Check permission and prompt user to change setting if its not turned on
    if([[AVAudioSession sharedInstance] respondsToSelector:@selector(requestRecordPermission:)])
    {
        [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted)
         {
             if (granted) {
                 // Microphone enabled code
                 NSLog(@"HotelViewController: Microphone is enabled..");
                 //MOB-15802 - Show search window in main thread.
                 dispatch_async(dispatch_get_main_queue(), ^{
                     [self  showVoiceSearchVC];
                 });
             }
             else {
                 // Microphone disabled code
                 NSLog(@"HotelViewController: Microphone is disabled..");
                 
                 // We're in a background thread here, so jump to main thread to do UI work.
                 dispatch_async(dispatch_get_main_queue(), ^{
                     [[[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Microphone Access Denied"]
                                                 message:[Localizer getLocalizedText:@"This feature requires access"]
                                                delegate:nil
                                       cancelButtonTitle:@"Dismiss"
                                       otherButtonTitles:nil] show];
                     // Return without doing anything.
                     return ;
                 }); // End alert code
             }
         }]; // End requestRecordPermission
        
    }
    else
    {
     	NSLog(@"HotelViewController: iOS6 - Mic requestRecordPermission not found ");
        [self  showVoiceSearchVC];
    }
    

}

-(void) showVoiceSearchVC
{
    UINavigationController *nav = [[UIStoryboard storyboardWithName:@"EvaVoiceSearch_iPhone" bundle:nil]
                                    instantiateInitialViewController];
    
    EvaVoiceSearchViewController *c = [nav viewControllers][0];
    
    c.category = self.category;
    
    [self presentViewController:nav animated:YES completion:nil];
}

- (void)dismissEVASearch:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}

 */

@end
