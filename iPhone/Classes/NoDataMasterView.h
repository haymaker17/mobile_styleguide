//
//  NoDataMasterView.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@class MobileViewController;
@class NoDataMasterView;

@protocol NoDataMasterViewDelegate <NSObject>
-(void) actionOnNoData:(id)sender;
-(BOOL) canShowActionOnNoData;
-(BOOL) adjustNoDataView:(NoDataMasterView*) negView;  // Return whether to hide toolbar
-(BOOL) canShowOfflineTitleForNoDataView;

-(NSString*) titleForNoDataView;
-(NSString*) buttonTitleForNoDataView;
-(NSString*) instructionForNoDataView;
-(int) baseViewState;
@optional
-(NSString*) imageForNoDataView;
-(BOOL) allowActionWhileOffline;
@end

@interface NoDataMasterView : UIView {
    id <NoDataMasterViewDelegate> __weak delegate;
}
@property (nonatomic, weak) id <NoDataMasterViewDelegate> delegate;
@property (nonatomic,strong) IBOutlet UILabel *titleLbl;
@property (nonatomic,strong) IBOutlet UIImageView *iconImg;
@property (nonatomic,strong) IBOutlet UIButton *actionBtn;
@property (nonatomic,strong) IBOutlet UILabel *instructionLbl;

- (void)prepareSubviews;
- (id)initWithNib;
@end
