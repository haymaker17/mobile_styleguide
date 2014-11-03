//
//  TravelWaitViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 7/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DistractorImageView.h"

@interface TravelWaitViewController : UIViewController

@property (weak, nonatomic) IBOutlet DistractorImageView *distractor;
@property (weak, nonatomic) IBOutlet UILabel *caption;
@property (strong, nonatomic) NSString *captionText;

+ (void)showFullScreeWithText:(NSString*)text animated:(BOOL)animated;
 // Default
+ (void)showWithText:(NSString*)text animated:(BOOL)animated;
+ (void)showTransparentWithText:(NSString*)text animated:(BOOL)animated;

+ (void)hideAnimated:(BOOL)animated withCompletionBlock:(void(^)())completion;

@end
