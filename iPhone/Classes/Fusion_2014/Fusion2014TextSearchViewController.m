//
//  Fusion2014TextSearchViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "Fusion2014TextSearchViewController.h"
#import "CXConversationManager.h"
#import "CXSpeechBubbleView.h"
#import "CXWaitViewController.h"
#import "EvaJsonResponseHandler.h"
#import "Fusion14HotelSearchResultsViewController.h"
#import "Fusion14FlightSearchResultsViewController.h"
// Commented out this code until the ConcurSDK is updated for 9.13
//#import "CTEFreeTextQueryAPI.h"

@interface Fusion2014TextSearchViewController ()

@property (assign) BOOL isNewSession;
@property (assign) BOOL shouldShowPrompt;
@property (assign) BOOL didPerformSearch;
@property (strong, nonatomic) NSString *searchString;
//@property (strong, nonatomic) CTEFreeTextQueryAPI *textQueryAPI;

@end

@implementation Fusion2014TextSearchViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    NSString *context = @"";
    NSString *scope = @"fh";
    
    if (self.category == EVA_HOTELS) {
        context = @"h";
    } else if (self.category == EVA_FLIGHTS) {
        context = @"f";
    }

//    self.textQueryAPI = [[CTEFreeTextQueryAPI alloc] initWithCurrentContext:context currentScope:scope] ;
    NSString *latitude = [NSString stringWithFormat:@"%f", [[GlobalLocationManager sharedInstance] currentLocation].coordinate.latitude ];
    NSString *longitude  = [NSString stringWithFormat:@"%f", [[GlobalLocationManager sharedInstance] currentLocation].coordinate.longitude ];
    DLog(@"setting latitude and longitude : %@ , %@", latitude,longitude);
//    [self.textQueryAPI setLocationLatitude:latitude longitude:longitude];
//    [self.textQueryAPI restartSession];     // Restart a new session everytime.
    // Start with a clean conversation
    [CXConversationManager.sharedInstance clear];

}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.navigationController.navigationBarHidden = YES;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - UITextViewDelegate

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
//    [self.textQueryAPI requestJSONForQuery:textField.text success:^(NSDictionary *jsonResponse) {
//        [self handleJson:jsonResponse];
//    } failure:^(CTEError *error) {
//        [self handleError:error];
//    }];
    // Do the stuff here
    
    // ======== show commented out message ======
    [self showStatementsFromHuman:textField.text andComputer:@"Text search is disabled currently" withSpeech:NO];
    textField.text = @"";
    return YES;
}


#pragma mark - Handle Json response
// Required: Called when receiving an error from Eva.
//
//- (void)handleError:(CTEError *)error {
//    DLog(@"evaDidFailWithError: %@", error.localizedDescription);
//    
////    [self logVoiceFailure];
//    
//    [self showErrorStatementsFromHuman:nil andComputer:@"Sorry I didn't quite get that."];
//}

-(void)handleJson:(NSDictionary *)jsonResponse
{
    DLog(@"Data from Eva %@", jsonResponse);
    
    EvaJsonResponseHandler *handler = [[EvaJsonResponseHandler alloc] initWithDict:jsonResponse evaSearchCategory:self.category];

    EvaApiReply *apireply = [handler parseResponse];
    
    NSString *response = [apireply getPendingQuestion];
    
    // Respond back to user with sayIt text there is some text. Action type is unknown by default.
    //
    
    // There is a pending question to the user.
    //
    if([handler isParseSuccess] && [response lengthIgnoreWhitespace]) {
        //self.isOpenQuestion = YES;
        
        [self showStatementsFromHuman:handler.input_text andComputer:response];
    } else if ([handler canProceedSearch]) {
        
        // Get the sayit text from the location pointed out by the flow -> relatedlocation -> sayit.
        //
        NSString *responsetext = [[NSString alloc]initWithFormat:@"Searching for %@", [apireply getFlowSayIt]];
        self.searchString  = [handler getSearchCriteria];
        
        
        // Set callback code here.
        // If we post the search request then the do the following on response from MWS
        //
        [handler setOnRespondToFoundData:^(NSMutableDictionary *pBag) {

            DLog(@"pBag = %@", pBag);
            [self dismissViewControllerAnimated:NO completion:nil];
            [self showSearchResults:pBag];
        }];
        
        [self showStatementsFromHuman:handler.input_text andComputer:responsetext withSpeech:NO];
        
        [self performSelector:@selector(showWaitView) withObject:nil afterDelay:1];
        
        [handler performSearch];
        
        self.didPerformSearch = YES;
    } else {
        // Reset everything.
        // TODO: Show more descriptive error messages. Right now we always show a generic message.
        //
        if([handler.input_text lengthIgnoreWhitespace]) {
            [self showErrorStatementsFromHuman:handler.input_text andComputer:response];
        }
        
        // Reset sesssion everytime there is an error.
        //
        //self.isNewSession = YES;
        
        NSDictionary *dict = @{@"Type": @"Hotel" , @"Worked" : @"No"};
        [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_SUCCESS withParameters:dict];
        
        DLog(@"Invalid search: Eva response had errors");
    }

}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"ConversationCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ConversationCell"];
    }
    
    NSInteger index = indexPath.row;
    
    CXStatement *statement = [CXConversationManager.sharedInstance statementAtIndex:index];
    
    CXSpeechBubbleView *speechBubble = [[CXSpeechBubbleView alloc]
                                        initWithFrame:CGRectMake(0, 0, self.tableView.frame.size.width, 90)
                                        andStatement:statement];
    
    speechBubble.backgroundColor = [UIColor whiteColor];
    
    // Clear out the cell.
    //
    for (UIView *v in cell.contentView.subviews) {
        [v removeFromSuperview];
    }
    
    // Add our only child (speech bubble).
    //
    [cell.contentView addSubview:speechBubble];
    
    cell.backgroundColor = [UIColor clearColor];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [CXConversationManager.sharedInstance numStatements];
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSInteger index = indexPath.row;
    
    CXStatement *statement = [CXConversationManager.sharedInstance statementAtIndex:index];
    
    float h = [CXSpeechBubbleView heightForText:statement.text withWidth:self.tableView.frame.size.width];
    
    // Add vertical padding. Magic numbers!!1
    //
    return h + 20;
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)scrollView {
    // Empty.
}

#pragma mark - Actions

- (IBAction)didTapCloseButton:(id)sender {
    //[self.navigationController popViewControllerAnimated:YES];
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Utility

- (void)addStatement:(CXStatement *)statement {
    [CXConversationManager.sharedInstance addStatement:statement];
    
    NSUInteger nextRow = [CXConversationManager.sharedInstance numStatements] - 1;
    
    NSIndexPath *path = [NSIndexPath indexPathForRow:nextRow inSection:0];
    
    [self.tableView beginUpdates];
    
    if (statement.participant == CXParticipantComputer) {
        [self.tableView insertRowsAtIndexPaths:@[path]
                              withRowAnimation:UITableViewRowAnimationLeft];
    } else {
        [self.tableView insertRowsAtIndexPaths:@[path]
                              withRowAnimation:UITableViewRowAnimationRight];
    }
    
    [self.tableView endUpdates];
    
    [self.tableView scrollToRowAtIndexPath:path
                          atScrollPosition:UITableViewScrollPositionBottom
                                  animated:YES];
}

- (NSString *)getCategoryName {
    NSString *categoryName;
    
    if (self.category == EVA_HOTELS) {
        categoryName = @"Hotel";
    } else if (self.category == EVA_FLIGHTS) {
        categoryName = @"Flight";
    } else {
        categoryName = @"Unknown";
    }
    
    return categoryName;
}

- (void)showErrorStatementsFromHuman:(NSString *)request andComputer:(NSString *)response {
    
    NSString *FUSION2014_ERROR_MESSAGE = @"Sorry I didn't quite get that.";

    // Show the error text even if the input text is not nil.
    //
    if ([request lengthIgnoreWhitespace]) {
        CXStatement *s = [[CXStatement alloc] initWithText:request fromParticipant:CXParticipantHuman];
        [self addStatement:s];
    }
    
    if ([response lengthIgnoreWhitespace]) {
        CXStatement *s = [[CXStatement alloc] initWithText:response fromParticipant:CXParticipantComputer];
        [self addStatement:s];
    } else {
        NSString *text = [Localizer getLocalizedText:FUSION2014_ERROR_MESSAGE];
        CXStatement *s = [[CXStatement alloc] initWithText:text fromParticipant:CXParticipantComputer];
        [self addStatement:s];
    }
}

- (void)showSearchResults:(NSMutableDictionary *)pBag {
     self.navigationController.navigationBarHidden = NO;
    
    if (self.category == EVA_HOTELS) {
        Fusion14HotelSearchResultsViewController *nextController =
        [[UIStoryboard storyboardWithName:@"Fusion14HotelSearchResultsViewController" bundle:nil] instantiateInitialViewController];
        Msg *msg = [[Msg alloc] init];
        
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        
        [nextController didProcessMessage:msg];
        [self.navigationController pushViewController:nextController animated:YES];
        
    } else if (self.category == EVA_FLIGHTS) {
        Fusion14FlightSearchResultsViewController *nextController =
        [[UIStoryboard storyboardWithName:@"Fusion14FlightSearchResults_iPhone" bundle:nil] instantiateInitialViewController];
        nextController.airShop = pBag[@"AIR_SHOP"];
        nextController.vendors = nextController.airShop.vendors;
        nextController.shouldGetAllResults = YES;
         [self.navigationController pushViewController:nextController animated:YES];
    }
    
    
    
}

- (void)showStatementsFromHuman:(NSString *)request andComputer:(NSString *)response {
    [self showStatementsFromHuman:request andComputer:response withSpeech:YES];
}

- (void)showStatementsFromHuman:(NSString *)request andComputer:(NSString *)response withSpeech:(BOOL)withSpeech {
    CXStatement *s1 = [[CXStatement alloc] init];
    s1.text = request;
    s1.participant = CXParticipantHuman;
    
    [self addStatement:s1];
    
    CXStatement *s2 = [[CXStatement alloc] init];
    s2.text = response;
    s2.participant = CXParticipantComputer;
    
    [self addStatement:s2];
    
}

- (void)showWaitView {
    [self performSegueWithIdentifier:@"text_wait_view_segue" sender:self];
}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"text_wait_view_segue"]) {
        // Set the caption text
        CXWaitViewController *waitView = (CXWaitViewController *)segue.destinationViewController ;
        waitView.captionText = [NSString stringWithFormat:@"Please wait while we search for %@ ", self.searchString] ;
        
    }
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
