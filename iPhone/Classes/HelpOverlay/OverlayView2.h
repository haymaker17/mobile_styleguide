//
//  OverlayView2.h
//  ConcurMobile
//
//  Created by ernest cho on 10/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol OverlayClickDelegate <NSObject>

-(void)buttonYesClicked;
-(void)buttonNoClicked;
-(void)buttonCloseClicked;

@end

@interface OverlayView2 : UIView

@property (nonatomic, weak) id<OverlayClickDelegate> delegate;
@property (strong, nonatomic) IBOutlet UIView *topView;
@property (strong, nonatomic) IBOutlet UIView *bottomView;

- (id)initWithNibNamed:(NSString *)nibName;

@end
