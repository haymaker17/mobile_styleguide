//
//  AdsViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AdsViewController : MobileViewController {
    UIButton  *adsView;
    int        adIndex;
    
    NSMutableDictionary *adsLookup;
}

@property (nonatomic, strong) NSMutableDictionary *adsLookup;
@property (nonatomic, strong) UIButton  *adsView;
@property (nonatomic, assign) int        adIndex;

+(AdsViewController*)sharedInstance;
-(void) updateAdsView;
@end
