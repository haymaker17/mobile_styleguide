//
//  EvaVoiceSearchViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 4/10/14.
//  Copyright (c) 2014 2014 Concur. All rights reserved.
//

#import <Eva/Eva.h>

#import "CXConversationManager.h"
#import "CXSpeechBubbleView.h"
#import "CXWaitViewController.h"
#import "EvaJsonResponseHandler.h"
#import "Fusion14HotelSearchResultsViewController.h"
#import "Fusion14FlightSearchResultsViewController.h"
#import "EvaVoiceSearchViewController.h"
#import "Fusion2014TextSearchViewController.h"
#import "AirShopResultsVC.h"
#import "Config.h"
#import "TravelWaitViewController.h"


NSString *FUSION2014_ERROR_MESSAGE = @"Sorry I didn't quite get that.";

typedef void (^SpeechDidFinishBlock)(void);

@interface EvaVoiceSearchViewController ()

@property (strong, nonatomic) AVSpeechSynthesizer *siri;
@property (assign) BOOL didPerformSearch;
@property (assign) BOOL isRecording;
@property (assign) BOOL isNewSession;
@property (assign) BOOL shouldShowPrompt;
@property (assign) BOOL isShowingSearchResults;
@property (assign) BOOL isOpenQuestion;
@property (strong, nonatomic) SpeechDidFinishBlock speechDidFinishBlock;
@property (strong, nonatomic) NSString *searchString;

@end

@implementation EvaVoiceSearchViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    if (self.isTextSearchQuery) {
        [self performSegueWithIdentifier:@"showTextSearch" sender:self];
        self.isTextSearchQuery = NO;
        return;
    }
    [Eva sharedInstance].scope = @"fh";
    if (self.category == EVA_HOTELS) {
        [Eva sharedInstance].context = @"h";
    } else {
        [Eva sharedInstance].context = @"f";
    }
    
    [[Eva sharedInstance] setDelegate:self];
    // Initialize the start and end sounds
    NSURL *beepSound   = [[NSBundle mainBundle] URLForResource: @"voice_high"
                                                 withExtension: @"aif"];
    NSURL *beepSound2   = [[NSBundle mainBundle] URLForResource: @"voice_low"
                                                  withExtension: @"aif"];
    
    [[Eva sharedInstance] setStartRecordAudioFile:beepSound];
    [[Eva sharedInstance] setVADEndRecordAudioFile:beepSound2];
    [[Eva sharedInstance] setRequestedEndRecordAudioFile:beepSound2];
    [[Eva sharedInstance] setCanceledRecordAudioFile:beepSound2];

    
    self.didPerformSearch = NO;
    self.isRecording = NO;
    self.shouldShowPrompt = YES;
    self.isShowingSearchResults = NO;
    self.isOpenQuestion = NO;
    // MOB-19021
    CGRect promptFrame = self.prompt.frame;
    promptFrame.origin.y += 50;
    self.prompt.frame = promptFrame;
    self.prompt.alpha = 0;

    // Play audio out through loudspeaker.
    //
    NSError *error;
    AVAudioSession* audioSession = [AVAudioSession sharedInstance];
    
    [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord
                        error:&error];
    
    // This breaks AirPlay. Investigating why, but removing doesn't seem to have any
    // adverse effects.
    //
//    [audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker
//                                    error:&error];
    
    // Start up "Siri".
    //
    self.siri = [[AVSpeechSynthesizer alloc] init];

    self.siri.delegate = self;
    
    self.tableView.alpha = 0;
    
    // Start with a clean conversation.
    //
    [CXConversationManager.sharedInstance clear];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    if (self.shouldShowPrompt) {
        self.shouldShowPrompt = NO;
        
        CGRect promptFrame = self.prompt.frame;
        promptFrame.origin.y -= 50;

        [UIView animateWithDuration:0.5
                              delay:0
                            options:UIViewAnimationOptionCurveEaseInOut
                         animations:^{
                             self.prompt.frame = promptFrame;
                             self.prompt.alpha = 1.0;
                         } completion:^(BOOL finished) {
                              [[Eva sharedInstance] setAPIkey:EVA_API_KEY
                              withSiteCode:EVA_SITE_CODE
                              withMicLevel:YES
                              withRecordingTimeout:7.0];
                             
                          }];
    }
    else
    {
        self.isRecording = NO;
        [self.micButton setUserInteractionEnabled:YES];
    }
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    self.isShowingSearchResults = NO;
    [self.micButton setState:CXPulseMicStateInactive];
    
    [self logSearchResults];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // Hide toolbar and navbar
    self.navigationController.navigationBarHidden = YES;
    [self.navigationController setToolbarHidden:YES animated:YES];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self.siri stopSpeakingAtBoundary:AVSpeechBoundaryImmediate];
    [[Eva sharedInstance] setCanceledRecordAudioFile:nil];
    [[Eva sharedInstance] cancelRecord];

    self.isRecording = NO;
    self.isNewSession = YES;
    self.view.userInteractionEnabled = YES;
    self.navigationController.navigationBarHidden = NO;
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
    
    speechBubble.backgroundColor = [UIColor blackColor];
    
    // Clear out the cell.
    //
    for (UIView *v in cell.contentView.subviews) {
        [v removeFromSuperview];
    }
    
    // Add our only child (speech bubble).
    //
    [cell.contentView addSubview:speechBubble];
    
    cell.backgroundColor = [UIColor blackColor];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [CXConversationManager.sharedInstance numStatements];
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {

    
    CXStatement *statement = [CXConversationManager.sharedInstance statementAtIndex:indexPath.row];
    
    float h = [CXSpeechBubbleView heightForText:statement.text withWidth:self.tableView.frame.size.width];

    // Add vertical padding. Magic numbers!!1
    //
    return h + 20;
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)scrollView {
    // Empty.
}

#pragma mark - AVAudioPlayerDelegate

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player
                       successfully:(BOOL)completed  {
    
    DLog(@"audioPlayerDidFinishPlaying::completed?%d", completed);
}

#pragma mark - AVSpeechSynthesizerDelegate

- (void)speechSynthesizer:(AVSpeechSynthesizer *)synthesizer didFinishSpeechUtterance:(AVSpeechUtterance *)utterance {
    NSLog(@"Done speaking. Restarting Eva. New session? %d", self.isNewSession);
    
    if (self.speechDidFinishBlock) {
        self.speechDidFinishBlock();
        self.speechDidFinishBlock = nil;
    }
}

#pragma mark - EvaDelegate

// Required: Called when receiving an error from Eva.
//
- (void)evaDidFailWithError:(NSError *)error {
    NSLog(@"evaDidFailWithError: %@", error.localizedDescription);
    
    [self logVoiceFailure];
    
    //MOB-19021 - in case Eva errors out before the prompt is hidden
    [self dismissPrompt];
    [self showStatementsFromHuman:nil andComputer:FUSION2014_ERROR_MESSAGE withBlock:^{
        self.isShowingSearchResults = NO;
        [self.micButton setState:CXPulseMicStateRecording];
        [[Eva sharedInstance] startRecord:self.isNewSession];
        [self.micButton setUserInteractionEnabled:YES];
    }];
}

// Required: Called when receiving valid data from Eva.
//
- (void)evaDidReceiveData:(NSData *)dataFromServer {
    
    self.isShowingSearchResults = NO;
    [self dismissPrompt];
    
    [self.micButton setState:CXPulseMicStateInactive];
    
    NSString* dataStr = [[NSString alloc] initWithData:dataFromServer encoding:NSASCIIStringEncoding];

    DLog(@"evaDidReceiveData::Data from Eva %@", dataStr);
    
    EvaJsonResponseHandler *handler = [[EvaJsonResponseHandler alloc] initWithData:dataFromServer
                                                                 evaSearchCategory:self.category];
    EvaApiReply *apireply = [handler parseResponse];
    
    NSString *response = [apireply getPendingQuestion];
    
    // Respond back to user with sayIt text there is some text. Action type is unknown by default.
    // There is a pending question to the user.
    //
    if([handler isParseSuccess] && [response lengthIgnoreWhitespace]) {
        
        self.isOpenQuestion = YES;
        self.isNewSession = NO;
        [self showStatementsFromHuman:handler.input_text andComputer:response withBlock:^{
            self.isRecording = YES;
            [self startVoiceControl];
        }];
    } else if ([handler canProceedSearch]) {
        
        self.isOpenQuestion = NO;
        // Get the sayit text from the location pointed out by the flow -> relatedlocation -> sayit.
        // [handler getSearchCriteria] returns a custom build search string, however if evature suggests we show the full response string
        // Showing full response text is too long for siri utterance.
        // NSString *responsetext = [[NSString alloc]initWithFormat:@"Searching for %@", [apireply getFlowSayIt]];
        self.searchString  = [handler getSearchCriteria];
        
        // Set callback code here.
        // If we post the search request then the do the following on response from MWS
        //
        [handler setOnRespondToFoundData:^(NSMutableDictionary *pBag) {
            
            DLog(@"pBag = %@", pBag);
            self.isShowingSearchResults = YES;
            [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil ];
            [self showSearchResults:pBag];
        }];
        
        [self showStatementsFromHuman:handler.input_text andComputer:[[NSString alloc]initWithFormat:@"Searching for %@", [apireply getFlowSayIt] ] withBlock:^{
            DLog(@"show wait view");
            [TravelWaitViewController showFullScreeWithText:[NSString stringWithFormat:@"Please wait while we search for %@ ", [apireply getFlowSayIt]] animated:YES];
            [self.micButton setUserInteractionEnabled:YES];
        }];
        [handler performSearch];
        self.didPerformSearch = YES;
    } else {
        DLog(@"Invalid search: Eva response had errors");
        
        NSDictionary *dict = @{@"Type": @"Hotel" , @"Worked" : @"No"};
        [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_SUCCESS withParameters:dict];
        
        [self showStatementsFromHuman:handler.input_text andComputer:response withBlock:^{
            self.isRecording = YES;
//            [self.voiceControlActiveNotifier play];
            [self startVoiceControl];
        }];
    }
}

// Optional: Called when recording. averagePower and peakPower are in decibels.
// Must be implemented if shouldSendMicLevel is TRUE.
//
- (void)evaMicLevelCallbackAverage:(float)averagePower andPeak:(float)peakPower {
    //NSLog(@"evaMicLevelCallbackAverage %f %f", averagePower, peakPower);
    
    [self.micButton setPowerLevel:averagePower];
}

// Optional: Called everytime the record stops.
// Must be implemented if shouldSendMicLevel is TRUE.
//
- (void)evaMicStopRecording {
    NSLog(@"evaMicStopRecording");
    
    [self.micButton setState:CXPulseMicStateWaiting];
    [self stopVoiceControl];
}

// Optional: Called when initiation process is complete after setting the API keys.
//
- (void)evaRecorderIsReady {
    ALog(@"evaRecorderIsReady::Starting new Session::");
    
    // TODO: This is C&P from mic button action. Refactor.
    //
    self.isNewSession = YES;
    // Evature is ready start the voice recording.
    [self startVoiceControl];
}

#pragma mark - Actions

- (IBAction)didTapCloseButton:(id)sender {
    [[Eva sharedInstance] cancelRecord];
//    [self dismissViewControllerAnimated:YES completion:nil];
    // Pop view since we will be pushing this viewcontroller instead of presenting
    // PresentViewController doesnt work nicely after reserve is complete.
    // Old search UI screens are temporary. 
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)didTapMicButton:(id)sender {

    if (self.isRecording) {
        [[Eva sharedInstance] stopRecord];
        [self stopVoiceControl];
    } else {
        self.isNewSession = YES;
        NSLog(@"new session? %d", self.isNewSession);
        [self.micButton setUserInteractionEnabled:NO];
        [self startVoiceControl];
    }
}

#pragma mark - Voice control

/*!
 * Start the listening to the voice input
 */
- (void)startVoiceControl {
    
    // This should be always run on main thread since this method might be used in callback thread.
    // not starting on main thread makes evature to create more than one session.
    dispatch_async(dispatch_get_main_queue(), ^{
        
        if (self.isOpenQuestion) {
            self.isNewSession = NO;
        } else {
            self.isNewSession = YES;
        }
        
        self.isRecording = YES;
        [self.micButton setState:CXPulseMicStateRecording];
        [self.micButton setUserInteractionEnabled:YES];
        
        [[Eva sharedInstance] startRecord:self.isNewSession];

        
    });
 }

/*!
 * stop voice control
 */
- (void)stopVoiceControl {
    self.isRecording = NO;
    [self.micButton setUserInteractionEnabled:NO];
}

#pragma mark - Utility

/*!
 * Inserts a speech text bubble to the list of statements in the voice interaction table view.
 */
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

/*!
 * Hides the the welcome prompt
 */
- (void)dismissPrompt {
    [UIView animateWithDuration:0.5
                          delay:0
                        options:UIViewAnimationOptionCurveEaseInOut
                     animations:^{
                         self.prompt.alpha = 0;
                         self.tableView.alpha = 1.0;
                     } completion:nil];
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

- (void)showSearchResults:(NSMutableDictionary *)pBag {
    self.navigationController.navigationBarHidden = NO;
    Msg *msg = [[Msg alloc] init];
     UIViewController *nextController = nil;
    
    if (self.category == EVA_HOTELS) {
       // Disabled Fusion14 stuff
//        if ([Config isNewHotelBooking]) {
//            nextController =
//            [[UIStoryboard storyboardWithName:@"Fusion14HotelSearchResultsViewController" bundle:nil] instantiateInitialViewController];
//        }
//        else
        {
            nextController = [[HotelSearchResultsViewController alloc] initWithNibName:@"HotelSearchResultsViewController" bundle:nil];
        }
        
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        
        [self.navigationController pushViewController:nextController animated:YES];
        //MOB-19089 : slight delay so the view is loaded properly , other wise self.navigationcontroller.view is nil and doesnt show the polling view.
        [nextController performSelector:@selector(didProcessMessage:) withObject:msg afterDelay:0.3];

    } else if (self.category == EVA_FLIGHTS) {
        UIViewController *nextController = nil;
        if ([Config isNewAirBooking])
        {
            Fusion14FlightSearchResultsViewController *fusionvc =
            [[UIStoryboard storyboardWithName:@"Fusion14FlightSearchResults_iPhone" bundle:nil] instantiateInitialViewController];
            fusionvc.airShop = pBag[@"AIR_SHOP"];
            fusionvc.vendors = fusionvc.airShop.vendors;
            fusionvc.shouldGetAllResults = YES;
            nextController = fusionvc;
        }
        else
        {
            AirShopResultsVC *vc = [[AirShopResultsVC alloc] initWithNibName:@"AirShopResultsVC" bundle:nil];
            AirShop *airShop = pBag[@"AIR_SHOP"];
            // TODO : handle tafields ??
            //    vc.taFields = self.taFields;
            vc.vendors = airShop.vendors;
            vc.airShop = airShop;
            nextController = vc;
            
        }
        [self.navigationController pushViewController:nextController animated:YES];
    }
 }

- (void)showStatementsFromHuman:(NSString *)request andComputer:(NSString *)response {
    [self showStatementsFromHuman:request andComputer:response withBlock:nil];
}

- (void)showStatementsFromHuman:(NSString *)request
                    andComputer:(NSString *)response
                      withBlock:(SpeechDidFinishBlock)speechDidFinishBlock {

    self.speechDidFinishBlock = speechDidFinishBlock;
    

    
    if ([request lengthIgnoreWhitespace]) {
        CXStatement *s1 = [[CXStatement alloc] init];
        s1.text = request;
        s1.participant = CXParticipantHuman;
        [self addStatement:s1];
    }

    CXStatement *s2 = [[CXStatement alloc] init];
    s2.participant = CXParticipantComputer;
    
    if ([response lengthIgnoreWhitespace]) {
        s2.text = response;
    } else {
        s2.text = [Localizer getLocalizedText:FUSION2014_ERROR_MESSAGE];
    }

    [self addStatement:s2];
    
    AVSpeechUtterance *u = [AVSpeechUtterance
                            speechUtteranceWithString:response];
    
    // Min rate = 0, default = 0.5, max = 1.0
    //
    // I like it a little less manic.
    //
    u.rate = 0.3f;
    
    [self.siri speakUtterance:u];
}

#pragma mark - Logging

- (void)logVoiceFailure {
    NSString *categoryName = [self getCategoryName];
    
    NSDictionary *dict = @{@"Type": categoryName, @"Error Type": @"Eva"};

    [Flurry logEvent:FLURRY_VOICE_BOOK_ERROR withParameters:dict];
}

- (void)logSearchResults {
    if(!self.didPerformSearch) {
        NSString *categoryName = [self getCategoryName];
        
        NSDictionary *dict = @{@"Type": categoryName};
        
        [Flurry logEvent:FLURRY_VOICE_BOOK_USAGE_CANCELLED withParameters:dict];
    }
}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"showTextSearch"]) {
        Fusion2014TextSearchViewController *nextviewcontroller = segue.destinationViewController;
        nextviewcontroller.category = self.category;
    }
}

@end
