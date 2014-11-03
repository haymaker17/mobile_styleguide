//
//  TableUtils.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TableUtils : NSObject

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                    forTable:(UITableView *)tableView;

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                   andDetail:(NSString *)detail
                                    forTable:(UITableView *)tableView;

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                   andDetail:(NSString *)detail
                                    forTable:(UITableView *)tableView
                                    required:(BOOL)required;

+ (BOOL)isSwitchControl:(id)sender;

+ (BOOL)switchValue:(id)sender;

+ (UITableViewCell *)toggleCellWithLabel:(NSString *)label
                                forTable:(UITableView *)tableView
                               withValue:(BOOL)val
                                onTarget:(id)target
                             andSelector:(SEL)selector;

@end
