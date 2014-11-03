//
//  IgniteUserSearchData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteUserSearchData : MsgResponder
{
    NSString        *searchString;
    NSMutableArray  *searchResults;
}

@property (nonatomic, copy)   NSString          *searchString;
@property (nonatomic, strong) NSMutableArray    *searchResults;

@end
