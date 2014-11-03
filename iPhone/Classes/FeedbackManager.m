//
//  FeedbackWidget.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ATConnect.h"
#import "ATAppRatingFlow.h"
#import "ATAppRatingMetrics.h"
#import "ATMessageCenterMetrics.h"
#import "FeedbackManager.h"

@interface FeedbackManager ()
@property (strong, nonatomic) ATConnect *connection;
@property (copy) FeedbackResponseBlock responseBlock;
@end

@implementation FeedbackManager

+ (FeedbackManager *)sharedInstance {
    static FeedbackManager *_sharedInstance = nil;
    static dispatch_once_t pred;
    
    dispatch_once(&pred, ^{
        _sharedInstance = [[FeedbackManager alloc] init];
    });
    
    return _sharedInstance;
}

// Pretty simple right now - just hide the email field and set
// email address to the user's username.
//
- (BOOL)configure {
    ATConnect *connect = [ATConnect sharedConnection];

    connect.showEmailField = NO;
    connect.initialUserEmailAddress = [ExSystem sharedInstance].userName;
    
    return [self configured];
}

- (BOOL)configured {
    ATConnect *connect = [ATConnect sharedConnection];
    
    return connect.initialUserEmailAddress != nil;
}

- (id)init {
    self = [super init];
    
    if (self) {
        // Empty.
    }
    
    return self;
}

// To be called once by client to set up initial operation.
//
- (void)setup {
    ATConnect *connect = [ATConnect sharedConnection];
    
    connect.apiKey = @"2a4d9d876b481b64c9481db061e4028f716ea74ac57cab35e8a48a2cee9ba7a2";
    
    connect.initiallyUseMessageCenter = NO;
    
    // Apptentive broadcasts thee notifications out when user clicks button
    // on a feedback dialog. Start listening for them.
    //
    /*
     [[NSNotificationCenter defaultCenter] addObserver:self
     selector:@selector(didClickEnjoymentButton:)
     name:ATAppRatingDidClickEnjoymentButtonNotification
     object:nil];
     */
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didClickRatingButton:)
                                                 name:ATAppRatingDidClickRatingButtonNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didCloseFeedbackButton:)
                                                 name:ATMessageCenterIntroThankYouDidCloseNotification
                                               object:nil];
    
    /*
     * User going to message center. We don't care about this.
     *
     [[NSNotificationCenter defaultCenter] addObserver:self
     selector:@selector(didCloseFeedbackButton:)
     name:ATMessageCenterIntroThankYouHitMessagesNotification
     object:nil];
     */
    
    // When user clicks "Done" on message center.
    //
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didCloseFeedbackButton:)
                                                 name:ATMessageCenterDidHideNotification
                                               object:nil];
}

- (void)didClickEnjoymentButton:(NSNotification *)notification {
    //NSLog(@"didClickEnjoymentButton");
    
    //NSNumber *buttonClicked = [notification.userInfo objectForKey:ATAppRatingButtonTypeKey];
    
    // If user clicked no, then we're done with this interaction so
    // we send response back to caller so they can get on with life.
    //
    // If user clicked yes then we now have to get through the rating
    // interaction. So we're not done yet.
    //
    
    // "NO" means that we pop up the feedback form, so don't invoke block here.
    //
    //if (buttonClicked.intValue == ATAppRatingEnjoymentButtonTypeNo) {
    //    [self invokeResponseBlock];
    //}
}

- (void)didClickRatingButton:(NSNotification *)notification {
    //NSLog(@"didClickRatingButton");
    
    // Once user clicks through this interaction then we're done.
    // Send back response now.
    //
    [self invokeResponseBlock];
}

- (void)didCloseFeedbackButton:(NSNotification *)notification {
    NSLog(@"didCloseFeedbackButton");
    
    // Once user clicks through this interaction then we're done.
    // Send back response now.
    //
    [self invokeResponseBlock];
}

- (void)invokeResponseBlock {
    if (self.responseBlock != nil) {
        self.responseBlock();
        self.responseBlock = nil;
    }
}

- (void)requestRatingFromViewController:(UIViewController *)vc
                              withBlock:(FeedbackResponseBlock)responseBlock {
    
    if (self.showRatingOnNextView) {
        self.showRatingOnNextView = NO;
    }
    
    self.responseBlock = responseBlock;
    
    // If we're not configured yet then try to do that here.
    // If we can't then bail.
    //
    if (![self configured]) {
        if (![self configure]) {
            DLog(@"Unable to set local configuration.");
            [self invokeResponseBlock];
            return;
        }
    }
    
    ATAppRatingFlow *flow = [ATAppRatingFlow sharedRatingFlowWithAppID:@"335023774"];
    
    // Private method. But we need to know if the dialog will not be displayed - we
    // don't get any notifications about this, so we check now. If no feedback dialog
    // then go ahead and invoke the block and return.
    //
    if ([flow respondsToSelector:@selector(shouldShowDialog)]) {
        BOOL shouldShowDialog = [flow performSelector:@selector(shouldShowDialog)];
        if (!shouldShowDialog) {
            [self invokeResponseBlock];
            return;
        }
    }
    
    // Logging a "significant event" since the Apptentive predicate check
    // seems to puke unless we have *something* set to >0. Ugh.
    //
    [flow logSignificantEvent];
    
    [flow showRatingFlowFromViewControllerIfConditionsAreMet:vc];

    // If we have got here, make a call to invoke the response block, just incase it has not already
    // been invoked. For the record, I did try to use observers to handle this, but apptentive does not
    // trigger enough events in the right places.
    [self invokeResponseBlock];
}

@end
