//
//  OfferMultiLinkVC.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/3/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface OfferMultiLinkVC : MobileViewController  <UITableViewDelegate, UITableViewDataSource> {
    UITableView					*tableList;
    NSMutableArray				*links;
    UIImage                     *icon;
}

@property (nonatomic, strong) IBOutlet UITableView  *tableList;
@property (nonatomic, strong) NSMutableArray        *links;
@property (nonatomic, strong) UIImage               *icon;
@end
