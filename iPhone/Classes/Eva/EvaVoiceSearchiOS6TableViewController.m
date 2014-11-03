//
//  EvaVoiceSearchiOS6TableViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/8/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// State transitions could be better modularized.
// Handle the following error cases
//  -> when no sayit returned.
//  -> when location returns nil.

#import "EvaVoiceSearchiOS6TableViewController.h"
#import "EvaJsonResponseHandler.h"
#import "HotelSearchResultsViewController.h"
#import "DataConstants.h"
#import "LabelConstants.h"
#import <QuartzCore/QuartzCore.h>

#import "EvaMessageCell.h"
#import "EvaHeaderCell.h"
#import "CCMicControl.h"
#import "Config.h"
#import "AirShopResultsVC.h"
#import "Fusion14HotelSearchResultsViewController.h"

/*
 * to be refactored in a blockkit category
 */

@interface CCSpeechUtterance : AVSpeechUtterance
@property (nonatomic,copy) void (^completion)(void);
@end

@implementation CCSpeechUtterance
@end



static NSString *const kUserMessage = @"USER";
static NSString *const kMachineMessage = @"MACHINE";
static NSString *const kMessage = @"MESSAGE";
static NSString *const kSender = @"SENDER";
static NSString *const kEvaPendingRequestsKey = @"EvaPendingRequests";

static NSString *const kDefaltErrMessage = @"Sorry I didn't quite get that.";
static NSString *const kDefaltResetMessage = @"Ok let's start over. What Hotel can I find for you";

/*
 * see updateMicWithEvent for state machine implementation
 */

typedef NS_ENUM(NSUInteger, MicStatus) {
    kMicPaused,
    kMicRecording,
    kMicProcessing
};

typedef NS_ENUM(NSUInteger, MicEvents) {
    kMicTapped,
    kMicDone
};

@interface EvaVoiceSearchiOS6TableViewController ()

@property (nonatomic,strong) NSMutableArray *messages;
@property (nonatomic,strong) NSMutableArray *messagesMoks;
@property (nonatomic,strong) CCMicControl *micControl;
@property CGRect micFrame;
@property MicStatus micStatus;

@property (nonatomic,strong) AVPlayer *player;
@property (nonatomic,copy) void(^onPlayerDidReachEnd)(void);

@property (nonatomic,strong) NSDate *silenceStart;
@property (nonatomic,assign) float soundFloor;
@property (nonatomic,strong)  EvaJsonResponseHandler *handler;
@property BOOL didPerformSearch;
@property BOOL isNewSession;
@property BOOL isOpenQuestion;

@property (nonatomic,strong) NSDate *tic;

@property BOOL dispatchOnce;         // First time initialize evature, should use frame work call back.
@property (nonatomic,strong) AVSpeechSynthesizer *synthesizer;

@end

@implementation EvaVoiceSearchiOS6TableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

// TODO: it's probably better to create a category for floating view alligned at the bottom  

- (void) observeValueForKeyPath:(NSString*)keyPath ofObject:(id)object change:(NSDictionary*)change context:(void*)context {
    if ([keyPath isEqual:@"tableView.frame"]) {
        self.micFrame = CGRectMake(0,self.tableView.bounds.size.height-150 ,self.tableView.bounds.size.width , 150);
        [self scrollViewDidScroll:self.tableView];
    }
}

// Debug Statements for tracking time
// Delete before checkin.
- (void)printTic:(NSString *) comment {
    DLog(@"----> %.5f %@",-[self.tic timeIntervalSinceNow],comment);
}

#pragma mark Viewcontroller delegates
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.tic = [NSDate date];
    self.synthesizer = [[AVSpeechSynthesizer alloc] init];
    self.synthesizer.delegate = self;
    
    // for flurry - check if user used the search or simply cancelled the search.
    self.didPerformSearch = NO;
    
    // this will drop as we see lower dbs
    self.soundFloor = 0.0f;
        
    self.micControl = [[CCMicControl alloc] initWithFrame:CGRectZero];
    [self.micControl addTarget:self action:@selector(btnStartRecord:) forControlEvents:UIControlEventTouchUpInside];
    [self.micControl.message setHidden:YES];
    
    self.micStatus = kMicProcessing;
    [self.tableView addSubview:self.micControl];
    
    [self setMessagesMoks:[@[@"one",@"two",@"asdasdas \n asdsad \n asdsad",@"sadsad"]mutableCopy]];
    [self setMessages:[NSMutableArray arrayWithCapacity:1]];
    // Set the api_key asyncly so it wont delay the modal display
    dispatch_async(dispatch_get_main_queue(), ^{
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            self.dispatchOnce = [[Eva sharedInstance] setAPIkey:EVA_API_KEY withSiteCode:EVA_SITE_CODE withMicLevel:YES withRecordingTimeout:9.0f];
            if (!self.dispatchOnce) {
                ALog(@"evature did not initialized correctly");
                [self dismissViewControllerAnimated:YES completion:nil];
            }
        });
        [Eva sharedInstance].scope = @"fh";
        switch (self.inputSearchCategory) {
            case EVA_HOTELS:
                [Eva sharedInstance].context = @"h";
                
                break;
            case EVA_FLIGHTS:
                [Eva sharedInstance].context = @"f";
                break;
            default:
                break;
        }
        [self setEvaModule:[Eva sharedInstance]];
        self.evaModule.delegate = self;
        self.micStatus = kMicPaused;
        self.isNewSession = YES;

        // MOB-17662 Don't call following if codes it is initialzing Evature
        // use the automatically callback provide by their API
        if (!self.dispatchOnce)
        {
            // start recording immidiately
            dispatch_async(dispatch_get_main_queue(), ^{
                [self updateMicWithEvent:kMicTapped];
            });
        }
    });
    
    [self.navigationController setToolbarHidden:YES animated:NO];
    if ([Config isNewTravel]) {
        self.tableView.backgroundColor = [UIColor blackConcur];
    }
    else
        self.tableView.backgroundColor = [[UIColor alloc]initWithPatternImage:[UIImage imageNamed:@"voice_background"]];

    [self.cancel setTarget:self];
    [self.cancel setAction:@selector(closeView:)];

//    if ([UIDevice isPad])
//    {
//        [self.cancel setTarget:self];
//        [self.cancel setAction:@selector(closeView:)];
//    }

    [self setTableHeaderNote];
}


-(void)closeView:(id)sender
{
    [self.navigationController setToolbarHidden:NO animated:NO];
  	[self.navigationController popViewControllerAnimated:NO];

}
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.tableView setTableFooterView:[[UIView alloc] initWithFrame:CGRectMake(0, 0, self.tableView.bounds.size.width, 200)]];
    self.micFrame = CGRectMake(0,self.tableView.bounds.size.height-150 ,self.tableView.bounds.size.width , 150);
    [self scrollViewDidScroll:self.tableView];
    
    [self addObserver:self forKeyPath:@"tableView.frame" options:NSKeyValueObservingOptionOld context:nil];
    self.isNewSession = YES;
    //[self.messages removeAllObjects];
    [self.tableView reloadData];
    
    //[self.tableView layoutSubviews];
    [self.navigationController setToolbarHidden:YES animated:NO];

}

- (void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    self.micFrame = CGRectMake(0,self.tableView.bounds.size.height-150 ,self.tableView.bounds.size.width , 150);
    [self scrollViewDidScroll:self.tableView];

}

- (void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [self removeObserver:self forKeyPath:@"tableView.frame"];

    // do not stop playback if while showing searchresults
//    if (self.player) {
//        [self.player pause];
//        [self setPlayer:nil];
//    }

    // Cancel record session
    // Incase ay pending sessions they will be closed. 
    [self.evaModule cancelRecord];
    // incase user navigated out of this view while recording or processing.
    if (self.micStatus == kMicRecording || self.micStatus == kMicProcessing)
    {
        // Incase view disappeared then reset the stuff.
        [self.handler setOnRespondToFoundData:nil];
        [self updateMicWithEvent:kMicDone];
        double delayInSeconds = 0.5;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            Eva *tmp =[Eva sharedInstance];
            NSURLConnection *connection = [tmp performSelector:@selector(connection)];
            [connection cancel];
            DLog(@"cancelling  %@", [tmp performSelector:@selector(connection)]) ;
        });
    }
    // Reset the message list and start fresh. 
   // [self.messages removeAllObjects];
    
    // Add flurry 
    if(!self.didPerformSearch)
    {
        NSDictionary *dict = @{@"Type": @"Hotel"};
        [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_CANCELLED withParameters:dict];
    }

}

- (void) viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)viewWillUnload {
     [self setEvaModule:nil];
     [super viewWillUnload];
}

- (void)viewDidUnload {
    [self setEvaModule:nil];
    [self setMessages:nil];
    [super viewDidUnload];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
// Dispose of any resources that can be recreated.
}


#pragma mark - Show UI messages


- (void)showResponse:(NSString*)sayItText inputText:(NSString *)inputText
{
    if (inputText) {
        [self appendMessage:inputText from:kUserMessage];
    }
    if(sayItText)
    {
        [self appendMessage:sayItText from:kMachineMessage];
    }
    return;
    
}

-(void) showErrorResponse:(NSString *)inputText
{
    [self showErrorResponse:inputText errMsg:nil];
}

-(void) showErrorResponse:(NSString *)inputText errMsg:(NSString *)errMsg
{
    // Show the error text even if the input text is not nil.
    if ([inputText lengthIgnoreWhitespace]) {
        [self appendMessage:inputText from:kUserMessage];
    }
    if([errMsg lengthIgnoreWhitespace])
        [self appendMessage:errMsg from:kMachineMessage];
    else
        [self appendMessage:[Localizer getLocalizedText:kDefaltErrMessage] from:kMachineMessage];

    
}

#pragma mark - button action

- (void)appendMessage:(NSString*)message from:(NSString*)from{
    [self.messages addObject:@{kMessage :message, kSender:from}];
    
    if ([kMachineMessage isEqualToString:from]) {
        [self.tableView insertRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:([self.messages count]-1) inSection:0]] withRowAnimation:UITableViewRowAnimationLeft];
        
        if ([ExSystem is7Plus]) {
            [self speakSentence:message completion:^{
                if(self.isOpenQuestion)
                {
                    [self updateMicWithEvent:kMicDone];
                    [self updateMicWithEvent:kMicTapped];
                    self.isOpenQuestion = NO;
                }
                
            }];
        }
        else {
            [self say:message onDidReachEnd:nil];
        }
    } else {
        [self.tableView insertRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:([self.messages count]-1) inSection:0]] withRowAnimation:UITableViewRowAnimationRight];
    }
    //MOB-15386 - Fix for iOS7 works for iOS 6.1 also
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:([self.messages count]-1) inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
        [self setTableHeaderNote];
    });
}


- (IBAction)btnStartRecord:(id)sender {
    if (self.player) {
        [self.player pause];
    }
    
    if (self.synthesizer) {
        [self.synthesizer pauseSpeakingAtBoundary:AVSpeechBoundaryWord];
    }
    
    [self.micControl.message setHidden:YES];

    [self updateMicWithEvent:kMicTapped];
    // Test code - Delete this later Move these to unit tests
//    NSString *filePath = @"/Users/pavana/Downloads/EvaJsonSamples/eva_airFlow2.json"; //@"/Users/pavana/Downloads/EvaJsonSamples/hotelflow.json";
//    //création d'un string avec le contenu du JSON
//    NSData *myJSON = [[NSData alloc] initWithContentsOfFile:filePath ];
//
//    EvaJsonResponseHandler *handler = [[EvaJsonResponseHandler alloc]initWithData:myJSON evaSearchCategory:EVA_FLIGHTS];
//    [handler setOnRespondToFoundData:^(NSMutableDictionary *pbag) {
//        //[self showLabelWithText:@"Found search results"];
//        DLog(@"pbag : %@",pbag);
//        [self showSearchResults:pbag];
//    }];
//
//    [handler parseResponse];
//    NSString *searchString  = [handler getSearchCriteria];
//    DLog(@"searchString : %@",searchString);
//    [handler performSearch];
//    self.didPerformSearch = YES;
}

// Handle mic status based on events. 
- (void)updateMicWithEvent:(MicEvents)event{
    
    @synchronized(self.micControl){
        
    switch (event) {
            case kMicTapped:
            {
                self.silenceStart = [NSDate date];

                if (self.micStatus == kMicPaused) {
                    [self.micControl start:NO withSound:YES onDidReachEnd:^{
                        [self.evaModule startRecord:self.isNewSession];
                    }];
                    [self.micControl start:NO withSound:YES onDidReachEnd:nil];
                    [self.evaModule startRecord:self.isNewSession];
                    
                    DLog(@"kMicTapped::MicStatus::kMicPaused");
                    [self printTic:@"start Record"];
                    self.micStatus = kMicRecording;
                    // Use the same session unless we are starting afresh.
                    if(self.isNewSession)
                        self.isNewSession = FALSE;
                    
                } else if (self.micStatus == kMicRecording) {
                    [self.evaModule stopRecord];
                     [self printTic:@"STOP Record"];
                    [self.micControl setVolumeValue:0];
                    [self.micControl beginSpinning];
                    [self.micControl stop:NO withSound:YES];
                    DLog(@"kMicTapped::MicStatus::kMicRecording");
                    self.micStatus = kMicProcessing;
                    DLog(@"kMicTapped::MicStatus::Chaging to kMicProcessing");
                    
                } else if (self.micStatus == kMicProcessing) {
                    // wait for mic to finish processing
                    DLog(@"kMicTapped::MicStatus:: but is in kMicProcessing");
                }
            }
                break;
            case kMicDone:
            {
                if (self.micStatus == kMicProcessing) {
                    
                    [self.micControl setVolumeValue:0];
                    [self.micControl endSpinning];
                    DLog(@"kMicDone::MicStatus::kMicProcessing");
                    self.micStatus = kMicPaused;
                }
            }
                break;
            
            default:
                break;
        }
    }
}


#pragma mark - Eva Delegates

- (void)evaMicStopRecording
{
    DLog(@"::evaMicStopRecording::");
    // recording has stop
    [self printTic:@"evaMicStopRecording Method"];
    [self updateMicWithEvent:kMicTapped];
}


- (void)evaDidReceiveData:(NSData *)dataFromServer
{
    
    [self printTic:@"evaDidReceiveData Method"];
    NSString* dataStr = [[NSString alloc] initWithData:dataFromServer encoding:NSASCIIStringEncoding];
    DLog(@"Data from Eva %@", dataStr);
    
    EvaJsonResponseHandler *handler = [[EvaJsonResponseHandler alloc]initWithData:dataFromServer evaSearchCategory:self.inputSearchCategory];
    EvaApiReply *apireply = [handler parseResponse];

    [self printTic:@"evaDidReceiveData Method - Parse complete"];
 
    NSString *question = [apireply getPendingQuestion];
    // respond back to user with sayIt text there is some text. action type is unknown by default 
    if( [handler isParseSuccess] && [question lengthIgnoreWhitespace]  )  // There is a pending question to the user.
    {
        self.isOpenQuestion = YES;
        [self showResponse:question inputText:handler.input_text];
    }
    // Check if we can proceed to search.
    else if([handler canProceedSearch])
    {
        // get the sayit text from the location pointed out by the flow -> relatedlocation -> sayit.
        NSString *responsetext = [[NSString alloc]initWithFormat:@"Searching for %@", [apireply getFlowSayIt] ];
        NSString *searchString  = [handler getSearchCriteria];
        
        [self printTic:[NSString stringWithFormat:@"evaDidReceiveData getSearchCriteria:%@",searchString ]];
        [self showResponse:responsetext inputText:handler.input_text];
        
        // Set call back code here.
        // if we post the search request then the do the following on response from MWS
        [handler setOnRespondToFoundData:^(NSMutableDictionary *pbag) {
            DLog(@"pbag %@",pbag);
            [self printTic:@"evaDidReceiveData Respondtofounddata"];
            // if player is still playing then wait till player is done, set the call back code block
            // Weak reference so compiler doesnt give warnings.
            [self showSearchResults:pbag];
            // This code waits for the player to complete which is disabled for time being
            // Disalbed so audio continues to play while showing search results.
//            __weak EVASearchTableViewController *weakSelf = self;
//            if(self.player)
//            {
//                [self setOnPlayerDidReachEnd:^(void) {
//                      [weakSelf showSearchResults:pbag];
//                  }];
//            }
//            else
//            {
//                [self showSearchResults:pbag];
//            }
        }];

        [handler performSearch];
        self.didPerformSearch = YES;
    }
    else
    {
      
        // Reset everything
        // TODO : Show more descriptive error messages. Right now we always show a generic message.
        if([handler.input_text lengthIgnoreWhitespace] )
        {
            [self showErrorResponse:handler.input_text];
        }
        else
        {
            [self showErrorResponse:nil];
        }
        // Reset sesssion everytime there is an error
        self.isNewSession = YES;

        NSDictionary *dict = @{@"Type": @"Hotel" , @"Worked" : @"No"};
        [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_SUCCESS withParameters:dict];

        DLog(@"Invalid search : Eva response had errors");
        [self updateMicWithEvent:kMicDone];
    }
}


// Eva failed. show error message and unhide the 
- (void)evaDidFailWithError:(NSError *)error{
    DLog(@"evaDidFailWithError %@", [error localizedDescription]);
    
    NSDictionary *dict = @{@"Type ": @"Hotel", @"Error Type:" : @"Eva"};
    [Flurry logEvent:FLURRY_VOICE_BOOK_ERROR withParameters:dict];

    [self updateMicWithEvent:kMicDone];
    
    if ([[error localizedDescription] rangeOfString:@"offline"].location != NSNotFound) {
        // No localized text here - Since Eva is english only for now
        [self showErrorResponse:@"Sorry, The Internet connection appears to be offline. Please try again later."];
        return;
    }
    [self showErrorResponse:nil];
  }

- (void)evaMicLevelCallbackAverage: (float)averagePower andPeak: (float)peakPower{

    CGFloat volume = (74+averagePower)/74;
    [self.micControl setVolumeValue:volume];

 }


#pragma mark - Handle Search results

//
// Looks like we got response for search results from MWS
// Show corresponding ViewController
// 
-(void)showSearchResults:(NSMutableDictionary *)pBag
{

    // incase results show up first
    [self updateMicWithEvent:kMicDone];
    switch (self.inputSearchCategory) {
        case EVA_HOTELS:
            [self showHotelSearchResults:pBag];
            break;
           // TODO : for later use
        case EVA_FLIGHTS:
            [self showFlightSearchResults:pBag];
            break;
        default:
            break;
    }

}

//
// Check if there are any hotels.
// if there are none then show some meaningful message and stay in same view, else show search results view
//
-(void)showHotelSearchResults:(NSMutableDictionary *)pBag
{
    int hotelsCount = [pBag[@"TOTAL_COUNT"] intValue];
    if (hotelsCount == 0)
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_TITLE"]
                              message:[Localizer getLocalizedText:@"HOTEL_VIEW_NO_HOTELS_MESSAGE"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                              otherButtonTitles:nil];
        [alert show];
        // Reset the session so user can start his results.
        self.isNewSession = YES;
        return ;
    }
    
    NSDictionary *dict = @{@"Type": @"Hotel" , @"Worked" : @"Yes"};
    [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_SUCCESS withParameters:dict];

    HotelSearchResultsViewController *nextController = [[HotelSearchResultsViewController alloc] initWithNibName:@"HotelSearchResultsViewController" bundle:nil];
    Msg *msg = [[Msg alloc] init];
    msg.parameterBag = pBag;
    msg.idKey = @"SHORT_CIRCUIT";
    nextController.isVoiceBooking = YES;

    // Some debug statements
    // Sometimes the viewcontroller takes forever to show results. this might help in debugging the stuff
    [self printTic:@"showHotelSearchResults Before respondtofoundata"];
    [self.navigationController pushViewController:nextController animated:YES];
    [nextController respondToFoundData:msg];
    [self printTic:@"showHotelSearchResults After"];
}

/**
 Show flight search results
 */
-(void)showFlightSearchResults:(NSMutableDictionary*)pBag
{
    if (pBag[@"Error"] != nil) {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:pBag[@"ErrorCode"]
                              message:pBag[@"ErrorBody"] == nil ? @" Unknown error ": pBag[@"ErrorBody"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
                              otherButtonTitles:nil];
        [alert show];
        // Reset the session so user can start his results.
        self.isNewSession = YES;
        return ;

    }
    AirShopResultsVC *vc = [[AirShopResultsVC alloc] initWithNibName:@"AirShopResultsVC" bundle:nil];
    AirShop *airShop = pBag[@"AIR_SHOP"];
    // TODO : handle tafields
//    vc.taFields = self.taFields;
    vc.vendors = airShop.vendors;
    vc.airShop = airShop;
    [self.navigationController pushViewController:vc animated:YES];

}

//MOB-15386	: for iOS7 .
-(void)setTableHeaderNote
{
    EvaHeaderCell *headerCell = [self.tableView dequeueReusableCellWithIdentifier:@"HeaderCell"];
    switch (self.inputSearchCategory) {
        case EVA_HOTELS:
            self.title = [Localizer getLocalizedText:@"Hotel Search"];
            [headerCell.question setText:[Localizer getLocalizedText:@"What Hotel can I find for you?"]];
            [headerCell.example setText:[Localizer getLocalizedText:@"For Example: say Find a hotel in San Francisco for next week"]];
            break;
        case EVA_FLIGHTS:
            self.title = [Localizer getLocalizedText:@"Flight Search"];
            [headerCell.question setText:[Localizer getLocalizedText:@"What Flights can I find for you?"]];
            [headerCell.example setText:[Localizer getLocalizedText:@"For Example: say Find a Flight from San Francisco to New York for next week"]];
            break;
        default:
            break;
    }
    headerCell.backgroundColor = [UIColor clearColor];
    [self.tableView setTableHeaderView:headerCell];
    self.tableView.tableHeaderView.backgroundColor = [UIColor clearColor];
}


#pragma mark - Helper utility function

- (CGFloat)textViewHeightForAttributedText: (NSAttributedString*)text andWidth: (CGFloat)width
{
    UITextView *calculationView = [[UITextView alloc] init];
    [calculationView setAttributedText:text];
    CGSize size = [calculationView sizeThatFits:CGSizeMake(width, FLT_MAX)];
    return size.height;
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return [self.messages count];
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    
    
    EvaMessageCell *cell = [tableView dequeueReusableCellWithIdentifier:@"LeftCell"];
    cell.aMessage.frame = CGRectMake(0, 0, self.tableView.bounds.size.width-40, 20);
    [cell.aMessage setText:self.messages[indexPath.row][kMessage]];
    
    if ([ExSystem is7Plus])
        return [cell.aMessage sizeThatFits:CGSizeMake(cell.aMessage.frame.size.width, FLT_MAX)].height + 5;
    else
        return cell.aMessage.contentSize.height+5;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    EvaMessageCell *cell;
    CGFloat height;
    
    if ([kMachineMessage isEqualToString:self.messages[indexPath.row][kSender]]) {
        cell = [tableView dequeueReusableCellWithIdentifier:@"LeftCell" forIndexPath:indexPath];
        [cell.aMessage setText:self.messages[indexPath.row][kMessage]];
        if ([ExSystem is7Plus]) {
            height = [self textViewHeightForAttributedText:cell.aMessage.attributedText andWidth:cell.aMessage.frame.size.width];
            [cell.aMessage setFrame:CGRectMake(0, 0, cell.aMessage.bounds.size.width, height)];
        }
        else
            [cell.aMessage setFrame:CGRectMake(0, 0, cell.aMessage.bounds.size.width, cell.aMessage.contentSize.height)];
        
    }
    else {
        cell = [tableView dequeueReusableCellWithIdentifier:@"RightCell" forIndexPath:indexPath];
        [cell.aMessage setText:self.messages[indexPath.row][kMessage]];
        if ([ExSystem is7Plus]) {
            height = [self textViewHeightForAttributedText:cell.aMessage.attributedText andWidth:cell.aMessage.frame.size.width];
            [cell.aMessage setFrame:CGRectMake(40, 0, cell.aMessage.bounds.size.width, height)];
        }
        else
            [cell.aMessage setFrame:CGRectMake(40, 0, cell.aMessage.bounds.size.width, cell.aMessage.contentSize.height)];
        
    }
    // MOB-15386 : default color for iOS 7 is white so set the color explicitly
    cell.contentView.backgroundColor = [UIColor clearColor];
    [cell setBackgroundColor:[UIColor clearColor]];

    
    return cell;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView{
  
    [self.tableView bringSubviewToFront:self.micControl];
    [self.micControl setFrame:CGRectOffset(self.micFrame, scrollView.contentOffset.x, scrollView.contentOffset.y)];
}


#pragma mark - Text To Speech

- (void)say:(NSString*)sentence onDidReachEnd:(void (^) (void))onDidReachEndBlock {
    
 
    [self setOnPlayerDidReachEnd:onDidReachEndBlock];
    DLog(@"say %@",sentence);
    if (self.player) {
        [self.player pause];
        [self setPlayer:nil];
    }
    
    if (self.synthesizer) {
        [self.synthesizer pauseSpeakingAtBoundary:AVSpeechBoundaryWord];
    }

    // If there is no open question then just play till the second comma ie city and state/country
    if(! self.isOpenQuestion)
    {
        NSArray *tokens = [sentence componentsSeparatedByString:@","];
        NSString *cntry = nil;
        NSRange secondcomma = NSMakeRange(1, 0);

        if([tokens count] >= 3)
        {
            cntry = [tokens objectAtIndex:2];
            if([cntry lengthIgnoreWhitespace]> 1) {
                secondcomma = [sentence rangeOfString:cntry];
            }
            sentence = [sentence substringToIndex:secondcomma.location - 1];
        }
    }
   
    // temporary fix to avoid no speech when sentence are longer than 100 character
    if ([sentence length]>99) {

        for (int i = 99; i>0; i--) {
            if ([[NSCharacterSet punctuationCharacterSet] characterIsMember:[sentence characterAtIndex:i]] ) {
                sentence = [sentence substringToIndex:i];
                break;
            }
        }
    }
    

    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *encodedSentece = (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes( NULL, (CFStringRef)sentence,NULL,(CFStringRef)@"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8 ));
        NSString *queryURL = [NSString stringWithFormat:@"http://translate.google.com/translate_tts?tl=%@&q=%@",@"en",encodedSentece];
        DLog(@" QueryURL to say :%@ ",queryURL);
        [self setPlayer:[[AVPlayer alloc] initWithURL:[NSURL URLWithString:queryURL]]];

        UInt32 sessionCategory = kAudioSessionCategory_PlayAndRecord;
        AudioSessionSetProperty(kAudioSessionProperty_AudioCategory, sizeof(sessionCategory), &sessionCategory);
        
        UInt32 audioRouteOverride = kAudioSessionOverrideAudioRoute_Speaker;
        AudioSessionSetProperty (kAudioSessionProperty_OverrideAudioRoute,sizeof (audioRouteOverride),&audioRouteOverride);
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerItemDidReachEnd:) name:AVPlayerItemDidPlayToEndTimeNotification object:[self.player currentItem]];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerItemDidFailed:) name:AVPlayerItemFailedToPlayToEndTimeNotification object:[self.player currentItem]];

        
        [self.player play];


    });
    
}

- (void)playerItemDidReachEnd:(NSNotification *)notification {
    if (self.onPlayerDidReachEnd) {
        self.onPlayerDidReachEnd();
    }
    if(self.isOpenQuestion)
    {
        [self updateMicWithEvent:kMicDone];
        [self updateMicWithEvent:kMicTapped];
        self.isOpenQuestion = NO;
    }

}

- (void)playerItemDidFailed:(NSNotification *)notification {
    DLog(@"playerItemDidFailed current Item %@",[self.player.currentItem debugDescription]);
    DLog(@"playerItemDidFailed %@",[self.player.error localizedDescription]);
    //AVPlayerItem *p = [notification object];
    if (self.onPlayerDidReachEnd) {
        self.onPlayerDidReachEnd();
    }
    
    // Set the mic status even if player failed. 
    if(self.isOpenQuestion)
    {
        [self updateMicWithEvent:kMicDone];
        [self updateMicWithEvent:kMicTapped];
        self.isOpenQuestion = NO;
    }

}

#pragma mark - new tts

- (void)speakSentence:(NSString*)sentence completion: (void (^)(void))completion{
    CCSpeechUtterance *utterance = [[CCSpeechUtterance alloc] initWithString:sentence];
    [utterance setCompletion:completion];
    [utterance setRate:(AVSpeechUtteranceDefaultSpeechRate+AVSpeechUtteranceMinimumSpeechRate)/2];
    [self.synthesizer speakUtterance:utterance];
}

- (void)speechSynthesizer:(AVSpeechSynthesizer *)synthesizer didFinishSpeechUtterance:(AVSpeechUtterance *)utterance{
    ((CCSpeechUtterance*)utterance).completion();
    
}


#pragma mark -
#pragma Evature optionals
- (void)evaRecorderIsReady
{
    // start recording immidiately
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateMicWithEvent:kMicTapped];
    });
}



@end
