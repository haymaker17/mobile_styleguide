//
//  CCMenuMore.h
//  ConcurMobile
//
//  Created by laurent mery on 13/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
/**
 This menu could pop up when you tap on '...' button
 structure is a tableView, each line is an item menu
 menu is declared has a dictionnary and follow the format bellow
 could be closes on tap on modal view
 */

#import <Foundation/Foundation.h>

@interface CCMenuMore : NSObject

@property (nonatomic, strong) UITableView *tableViewMenu;

/*
 * menuItems is an array of dictionnary
 * @{@"title"           : NSString,
 *   @"segueIdentifier" : NSString,
 *   @"imageKey"        : NSString, [optionnal]
 *   @"height"          : NSInteger [optionnal]
 *  }
 */
-(id)initWithViewController:(UIViewController*)viewcontroller withMenuItems:(NSArray*)menuItems;

-(void)setHidden:(BOOL)hide;

@end
