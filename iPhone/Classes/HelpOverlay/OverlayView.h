//
//  OverlayView.h
//  ConcurMobile
//
//  Created by ernest cho on 2/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface OverlayView : UIView

@property (strong, nonatomic) IBOutlet UILabel *dismissText;
@property (strong, nonatomic) IBOutlet UILabel *menuHelpText;
@property (strong, nonatomic) IBOutlet UILabel *messageCenterText;
@property (strong, nonatomic) IBOutlet UILabel *travelHelpText;
@property (strong, nonatomic) IBOutlet UILabel *cameraHelpText;
@property (strong, nonatomic) IBOutlet UILabel *quickExpenseHelpText;
@property (strong, nonatomic) IBOutlet UIImageView *menuHelpImage;
@property (strong, nonatomic) IBOutlet UIImageView *messageCenterImage;
@property (strong, nonatomic) IBOutlet UIImageView *travelHelpImage;
@property (strong, nonatomic) IBOutlet UIImageView *cameraHelpImage;
@property (strong, nonatomic) IBOutlet UIImageView *quickExpenseHelpImage;

- (id)initForIPad;
- (void)touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event;
- (void)removeWithEffect:(UIView*)viewToRemove;
@end
    