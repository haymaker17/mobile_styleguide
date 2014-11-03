//
//  IgniteRecommendationVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "IgniteSegmentEditDelegate.h"
#import "EntitySegment.h"
#import "IgniteVendorAnnotation.h"

@interface IgniteRecommendationVC : MobileViewController
    <UITableViewDataSource, UITableViewDelegate>
{
    UITableView                     *tableList;
    UINavigationBar                 *navBar;
    UIToolbar                       *tBar;
    
    UIView                          *vwHeader;
    UILabel                         *lblVendorName;
    UILabel                         *lblAddress;
    UILabel                         *lblCity;
    UIButton                        *btnPhone;
    UIImageView                     *ivVendor;
    UIImageView                     *ivStarRating;
    
    UIView                          *vwReviewTitle;
    UILabel                         *lblReviewTitle;
    UIButton                        *btnWriteReview;
    
    EntitySegment                   *segment;
    NSMutableArray                  *reviews;
    IgniteVendorAnnotation          *vendor;
    
    id<IgniteSegmentEditDelegate>   __weak _delegate;    
}

@property (nonatomic, strong) IBOutlet UITableView          *tableList;
@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) IBOutlet UIToolbar            *tBar;

@property (nonatomic, strong) IBOutlet UIView               *vwHeader;
@property (nonatomic, strong) IBOutlet UILabel              *lblVendorName;
@property (nonatomic, strong) IBOutlet UILabel              *lblAddress;
@property (nonatomic, strong) IBOutlet UILabel              *lblCity;
@property (nonatomic, strong) IBOutlet UIButton             *btnPhone;
@property (nonatomic, strong) IBOutlet UIImageView          *ivVendor;
@property (nonatomic, strong) IBOutlet UIImageView          *ivStarRating;

@property (nonatomic, strong) IBOutlet UIView               *vwReviewTitle;
@property (nonatomic, strong) IBOutlet UILabel              *lblReviewTitle;
@property (nonatomic, strong) IBOutlet UIButton             *btnWriteReview;

@property (nonatomic, strong) EntitySegment                 *segment;
@property (nonatomic, strong) NSMutableArray                *reviews;
@property (nonatomic, strong) IgniteVendorAnnotation        *vendor;
@property (nonatomic, weak) id<IgniteSegmentEditDelegate>  delegate;

- (void)setSeedData:(id<IgniteSegmentEditDelegate>) del loc:(IgniteVendorAnnotation*)annotation seg:(EntitySegment*) seg;

@end
