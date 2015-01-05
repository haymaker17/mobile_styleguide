//
//  BaseDetailVC_iPad.h
//  ConcurMobile
//
//  Created by charlottef on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface ButtonDescriptor : NSObject
@property (copy, nonatomic) NSString *buttonId;
@property (copy, nonatomic) NSString *title;
+ (ButtonDescriptor*) buttonDescriptorWithId:(NSString*)btnId title:(NSString*)btnTitle;
@end

@interface BaseDetailVC_iPad : MobileViewController

@property (readonly, nonatomic) UIView              *leftPaneHeaderView;
@property (readonly, nonatomic) UIView              *leftPaneFooterView;
@property (strong, nonatomic) IBOutlet UITableView  *rightTableView;
@property (strong, nonatomic) IBOutlet UIView       *loadingView;
@property (strong, nonatomic) IBOutlet UILabel      *loadingLabel;
@property (strong, nonatomic) IBOutlet UIActivityIndicatorView      *activityView;

// Methods for subclasses to call
- (void) setButtonDescriptors:(NSArray*)descriptors;

// Methods for subclasses to modify (optional)
- (UIButton*) buttonAtIndex:(NSInteger)index;
- (UILabel*) labelOnButtonAtIndex:(int)index;
- (void) configureButtons;

// Methods for subclasses to override
- (UIView*) loadHeaderView;
- (UIView*) loadFooterView;

- (void) didPressButtonAtIndex:(NSInteger)buttonIndex withId:(NSString*)buttonId inRect:(CGRect)rect;

@end