//
//  FeedbackWidget.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

typedef void (^FeedbackResponseBlock)(void);

@interface FeedbackManager : NSObject

// Always use singleton method.
//
+ (FeedbackManager *)sharedInstance;

@property (assign) BOOL showRatingOnNextView;

- (void)requestRatingFromViewController:(UIViewController *)vc withBlock:(FeedbackResponseBlock)responseBlock;
- (void)setup;

@end
