//
//  AdView.h
//  ConcurMobile
//
//  Created by ernest cho on 3/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AdView : UIView

@property (strong, nonatomic) IBOutlet UIImageView *adImage;

- (BOOL)shouldShowAd;
- (void)touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event;

// adds self to a list of toolbar items. Includes a spacer so it lines up correctly.
- (NSMutableArray *)getToolbarItemsForIPhone;
- (NSMutableArray *)getToolbarItemsForIPadModal;

@end
