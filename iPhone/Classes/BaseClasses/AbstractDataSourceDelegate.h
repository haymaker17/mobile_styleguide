//
//  AbstractDataSourceDelegate.h
//  PastDestinations
//
//  Created by Pavan Adavi on 6/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class AbstractDataSource;

@protocol AbstractDataSourceSectionInfo

/* Name of the section
 */
@property (nonatomic, readonly) NSString *name;

/* Title of the section (used when displaying the index)
 */
@property (nonatomic, readonly) NSString *indexTitle;

/* Number of objects in section
 */
@property (nonatomic, readonly) NSUInteger numberOfObjects;

/* Returns the array of objects in the section.
 */
@property (nonatomic, readonly) NSArray *objects;

@end

@protocol AbstractDataSourceDelegate <NSObject>

enum {
	kDataSourceChangeInsert = 1,
	kDataSourceChangeDelete = 2,
	kDataSourceChangeMove = 3,
	kDataSourceChangeUpdate = 4
	
};
typedef NSUInteger DataSourceChangeType;

@optional
-(void)dataSourceWillChangeData:(AbstractDataSource *)dataSource ;

/* Notifies the delegate that a fetched object has been changed due to an add, remove, move, or update. Enables AbstractDataSource change tracking.
 controller - controller instance that noticed the change on its fetched objects
 anObject - changed object
 indexPath - indexPath of changed object (nil for inserts)
 type - indicates if the change was an insert, delete, move, or update
 newIndexPath - the destination path for inserted or moved objects, nil otherwise
 
 Changes are reported with the following heuristics:
 
 On Adds and Removes, only the Added/Removed object is reported. It's assumed that all objects that come after the affected object are also moved, but these moves are not reported.
 The Move object is reported when the changed attribute on the object is one of the sort descriptors used in the fetch request.  An update of the object is assumed in this case, but no separate update message is sent to the delegate.
 The Update object is reported when an object's state changes, and the changed attributes aren't part of the sort keys.
 */
@optional
- (void)dataSource:(AbstractDataSource *)dataSource didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(DataSourceChangeType)type newIndexPath:(NSIndexPath *)newIndexPath;

/* Notifies the delegate of added or removed sections.  Enables AbstractDataSource change tracking.
 
 controller - controller instance that noticed the change on its sections
 sectionInfo - changed section
 index - index of changed section
 type - indicates if the change was an insert or delete
 
 Changes on section info are reported before changes on fetchedObjects.
 */

@optional
- (void)dataSource:(AbstractDataSource *)dataSource didChangeSection:(id <AbstractDataSourceSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(DataSourceChangeType)type;
@optional
/* Notifies the delegate that all section and object changes have been sent.
 Providing an empty implementation will enable change tracking if you do not care about the individual callbacks.
 */
@optional
- (void)dataSourceDidChangeContent:(AbstractDataSource *)dataSource;

/* Asks the delegate to return the corresponding section index entry for a given section name.
 If this method isn't implemented by the delegate, the default implementation returns the capitalized first letter of the section name (seee AbstractDataSource sectionIndexTitleForSectionName:)
 Only needed if a section index is used.
 */

@optional
- (NSString *)dataSource:(AbstractDataSource *)dataSource sectionIndexTitleForSectionName:(NSString *)sectionName;

@end
