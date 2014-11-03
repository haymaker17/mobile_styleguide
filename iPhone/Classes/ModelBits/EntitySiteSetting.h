//
//  EntitySiteSetting.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySiteSetting : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * value;
@property (nonatomic, strong) NSString * type;
@property (nonatomic, strong) NSString * name;

@end
